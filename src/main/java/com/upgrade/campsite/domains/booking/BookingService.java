package com.upgrade.campsite.domains.booking;

import com.upgrade.campsite.constants.ErrorMessages;
import com.upgrade.campsite.dtos.BookingDTO;
import com.upgrade.campsite.dtos.ModifyBookingDTO;
import com.upgrade.campsite.exceptions.AlreadyBookedException;
import com.upgrade.campsite.exceptions.BookingCancelationException;
import com.upgrade.campsite.exceptions.BookingException;
import com.upgrade.campsite.exceptions.BookingFinishedException;
import com.upgrade.campsite.exceptions.InvalidInputException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingCacheService cachingService;

    public List<LocalDate> getAvailableDates(LocalDate startDate, LocalDate endDate) {
        // Validation
        // If any date is empty, get defaults
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(29);
        }

        // EndDate can't be before startDate
        if (startDate.isAfter(endDate)) {
            throw new InvalidInputException(ErrorMessages.INVALID_DATE_START_BEFORE_END);
        }

        // Get all dates between the two dates
        // Adding an extra day at the end as datesUntil is exclusive
        List<LocalDate> availableDates = startDate
                .datesUntil(endDate.plusDays(1))
                .collect(Collectors.toList());

        Set<LocalDate> bookedDates = getBookedDates(startDate, endDate);
        if (bookedDates != null && !bookedDates.isEmpty()) {
            // Remove from available the scheduled dates
            availableDates.removeAll(bookedDates);
        }

        return availableDates;
    }

    public BookingDTO getBooking(String bookingId) {
        Booking booking = getBookingById(bookingId);
        LocalDate maxDate = booking.getDate().stream()
                .max(Comparator.comparing(LocalDate::toEpochDay))
                .get();

        LocalDate minDate = booking.getDate().stream()
                .min(Comparator.comparing(LocalDate::toEpochDay))
                .get();

        return toBookingDTO(booking, minDate, maxDate);
    }

    public BookingDTO createNewBooking(BookingDTO newBooking) {
        // Valide Date Range
        LocalDate startDate = newBooking.getStartDate();
        LocalDate endDate = newBooking.getEndDate();
        validateDates(startDate, endDate);

        // Create Unique ID
        String bookingId = UUID.randomUUID().toString();
        newBooking.setBookingId(bookingId);

        // Get all dates between the two dates
        Set<LocalDate> desiredDates = getDatesBetween(startDate, endDate);

        // Create DB object
        Booking booking = Booking.builder()
                .bookingId(bookingId)
                .name(newBooking.getName())
                .email(newBooking.getEmail())
                .date(desiredDates)
                .build();

        try {
            booking = bookingRepository.save(booking);
            cachingService.addToCache(desiredDates);
        } catch (DataIntegrityViolationException ex) {
            throw new AlreadyBookedException(ErrorMessages.ALREADY_BOOKED);
        } catch (Exception ex) {
            log.error("Error during booking save.", ex);
            throw new BookingException(ErrorMessages.USER_FRIENDLY_GENERAL_ERROR);
        }

        return toBookingDTO(booking, startDate, endDate);
    }

    public BookingDTO modifyBooking(ModifyBookingDTO modifyBookingDTO) {
        // Valide Date Range
        LocalDate newStartDate = modifyBookingDTO.getStartDate();
        LocalDate newEndDate = modifyBookingDTO.getEndDate();
        validateDates(newStartDate, newEndDate);

        // Get booking
        Booking oldBooking = getBookingById(modifyBookingDTO.getBookingId());

        // Validate
        if (isDatesInPast(oldBooking.getDate())) {
            throw new BookingFinishedException("Can't modify a booking that has already passed.");
        }

        // Create new booking
        Set<LocalDate> newDesiredDates = getDatesBetween(newStartDate, newEndDate);
        Booking newBooking = Booking.builder()
                .id(oldBooking.getId())
                .bookingId(oldBooking.getBookingId())
                .name(oldBooking.getName())
                .email(oldBooking.getEmail())
                .date(newDesiredDates)
                .build();

        try {
            bookingRepository.save(newBooking);
            cachingService.updateCache(newDesiredDates, oldBooking.getDate());
        } catch (DataIntegrityViolationException ex) {
            throw new AlreadyBookedException(ErrorMessages.ALREADY_BOOKED);
        } catch (ObjectOptimisticLockingFailureException ex) {
            if (ex.getMostSpecificCause() instanceof StaleStateException) {
                throw new InvalidInputException(ErrorMessages.BOOKING_ID_NOT_FOUND);
            } else {
                log.error("Error during booking save.", ex);
                throw new BookingException(ErrorMessages.USER_FRIENDLY_GENERAL_ERROR);
            }
        } catch (Exception ex) {
            log.error("Error during booking save.", ex);
            throw new BookingException(ErrorMessages.USER_FRIENDLY_GENERAL_ERROR);
        }

        return toBookingDTO(newBooking, newStartDate, newEndDate);
    }

    public void deleteBooking(String bookingId) {
        // Get booking
        Booking booking = getBookingById(bookingId);

        // Validate
        if (isDatesInPast(booking.getDate())) {
            throw new BookingFinishedException("Can't delete a booking that has already passed.");
        }

        try {
            bookingRepository.delete(booking);
            cachingService.removeFromCache(booking.getDate());
        } catch (Exception ex) {
            log.error("Error during booking delete.", ex);
            throw new BookingCancelationException(ErrorMessages.USER_FRIENDLY_GENERAL_ERROR);
        }
    }

    public void resetCache() {
        try {
            // Delete all cache
            cachingService.clearCache();

            // Get all bookings for the future (which won't be more than 1 month)
            Set<LocalDate> bookedDates = bookingRepository.findScheduledDates(LocalDate.now(), LocalDate.MAX);

            // Update caching
            cachingService.addToCache(bookedDates);
        } catch (Exception ex) {
            log.error("Error during cache reset.", ex);
        }

    }

    private Booking getBookingById(String bookingId) throws InvalidInputException {
        Booking booking = bookingRepository.findByBookingId(bookingId);
        if (booking == null) {
            throw new InvalidInputException(ErrorMessages.BOOKING_ID_NOT_FOUND);
        }
        return booking;
    }

    // EndDate can't be before startDate
    // StartDate and EndDate can't be equals
    // Maximum of 3 days reservation
    // Reservation must be for more than 1 day and less than 30 days in the future
    private void validateDates(LocalDate startDate, LocalDate endDate) throws InvalidInputException {
        Long daysTillReservation = ChronoUnit.DAYS.between(LocalDate.now(), startDate);
        if (endDate.isBefore(startDate)
                || startDate.equals(endDate)
                || ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)) > 3 // Adding one day as between is exclusive
                || daysTillReservation < 1
                || daysTillReservation > 30) {

            throw new InvalidInputException(ErrorMessages.INVALID_BOOKING_DATES);
        }
    }

    private boolean isDatesInPast(Set<LocalDate> dates) {
        return dates.stream().anyMatch(d -> d.equals(LocalDate.now()) || d.isBefore(LocalDate.now()));
    }

    private BookingDTO toBookingDTO(Booking booking, LocalDate startDate, LocalDate endDate) {
        return BookingDTO.builder()
                .bookingId(booking.getBookingId())
                .name(booking.getName())
                .email(booking.getEmail())
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private Set<LocalDate> getBookedDates(LocalDate startDate, LocalDate endDate) {
        // Get from cache
        Set<LocalDate> cacheResult = cachingService.getAllFromCache();

        // If cache is empty, try to get from DB and update cache
        // Otherwise, just return the cache
        if (cacheResult == null || cacheResult.isEmpty()) {
            cacheResult = bookingRepository.findScheduledDates(startDate, endDate);
            cachingService.addToCache(cacheResult);
            return cacheResult;
        } else {
            return cacheResult;
        }
    }

    private Set<LocalDate> getDatesBetween(LocalDate newStartDate, LocalDate newEndDate) {
        // Get all dates between the two dates
        // Adding an extra day at the end as datesUntil is exclusive
        Set<LocalDate> newDesiredDates = newStartDate
                .datesUntil(newEndDate.plusDays(1))
                .collect(Collectors.toSet());
        return newDesiredDates;
    }

}
