package com.upgrade.campsite.services;

import com.upgrade.campsite.AbstractTest;
import com.upgrade.campsite.domains.booking.Booking;
import com.upgrade.campsite.dtos.BookingDTO;
import com.upgrade.campsite.dtos.ModifyBookingDTO;
import com.upgrade.campsite.exceptions.AlreadyBookedException;
import com.upgrade.campsite.exceptions.BookingFinishedException;
import com.upgrade.campsite.exceptions.InvalidInputException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class BookingServiceTests extends AbstractTest {

    @Before
    public void before() {
        deleteAll();
    }

    // ============================
    // = GET AVAILABLE DATES TESTS
    // ============================
    @Test
    public void givenExistingBookingShouldReturnEmptyAvailableDatesForSameBookingDates() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        BookingDTO dto = createBookingDTO(startDate, endDate);
        bookingService.createNewBooking(dto);

        List<String> availableDates = bookingService.getAvailableDates(startDate, endDate);

        assertTrue(availableDates.isEmpty());
    }

    @Test
    public void givenRequestForAvailableDatesWithEmptyDatesShouldReturnNext30Days() {
        List<String> availableDates = bookingService.getAvailableDates(null, null);
        assertThat(availableDates, hasSize(30));
    }

    @Test
    public void givenOneBookingOf3DaysShouldReturn27AvailableDates() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        bookingService.createNewBooking(createBookingDTO(startDate, endDate));

        List<String> availableDates = bookingService.getAvailableDates(null, null);

        assertThat(availableDates, hasSize(27));
    }

    // ============================
    // = BOOKING TESTS
    // ============================
    @Test(expected = InvalidInputException.class)
    public void givenEndDateBeforeStartDateShouldThrowException() {
        BookingDTO booking = createBookingDTO(
                LocalDate.now().plusDays(1),
                LocalDate.now());
        bookingService.createNewBooking(booking);
    }

    @Test(expected = InvalidInputException.class)
    public void givenBokingOfMoreThan3DaysShouldThrowException() {
        BookingDTO booking = createBookingDTO(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10));
        bookingService.createNewBooking(booking);
    }

    @Test(expected = InvalidInputException.class)
    public void givenTodayStartDateShouldThrowException() {
        BookingDTO booking = createBookingDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(10));
        bookingService.createNewBooking(booking);
    }

    @Test(expected = InvalidInputException.class)
    public void givenStartDateIsMoreThan30DaysInAdvanceShouldThrowException() {
        BookingDTO booking = createBookingDTO(
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(35));
        bookingService.createNewBooking(booking);
    }

    @Test(expected = AlreadyBookedException.class)
    public void givenExistingBookingANewOneWithSameDatesShouldThrowException() {
        Booking booking = createValidBooking();
        assertTrue(booking.getBookingId().equals(bookingRepository.findByBookingId(booking.getBookingId()).getBookingId()));

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        BookingDTO dto = createBookingDTO(startDate, endDate);
        bookingService.createNewBooking(dto);
    }

    @Test
    public void givenValidBookingShouldCallSaveMethod() {
        Booking booking = createValidBooking();
        assertTrue(booking.getBookingId().equals(bookingRepository.findByBookingId(booking.getBookingId()).getBookingId()));
    }

    // ============================
    // = MODIFY BOOKING TESTS
    // ============================
    @Test
    public void givenValidBookingShouldCallModifyMethod() {
        Booking booking = createValidBooking();
        ModifyBookingDTO modify = createBookingDTO(
                booking.getBookingId(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7));

        BookingDTO result = bookingService.modifyBooking(modify);
        assertTrue(result.getStartDate().equals(LocalDate.now().plusDays(5)));

        assertTrue(bookingService.getBooking(booking.getBookingId()).getStartDate().equals(LocalDate.now().plusDays(5)));
    }

    @Test(expected = InvalidInputException.class)
    public void givenModificationOfInvalidBookingIdShouldThrowException() {
        ModifyBookingDTO modify = createBookingDTO(
                UUID.randomUUID().toString(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7));
        bookingService.modifyBooking(modify);
    }

    @Test(expected = BookingFinishedException.class)
    public void givenModificationOfAlreadyFinishedBookingShouldThrowException() {
        Booking booking = createBooking(
                LocalDate.now().minusDays(6),
                LocalDate.now().minusDays(4));

        ModifyBookingDTO modify = createBookingDTO(
                booking.getBookingId(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7));

        bookingService.modifyBooking(modify);
    }

    // ============================
    // = DELETE BOOKING TESTS
    // ============================
    @Test(expected = InvalidInputException.class)
    public void givenDeletionOfInvalidBookingIdShouldThrowException() {
        bookingService.deleteBooking(UUID.randomUUID().toString());
    }

    @Test(expected = BookingFinishedException.class)
    public void givenDeletionOfAlreadyFinishedBookingShouldThrowException() {
        Booking booking = createBooking(
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(4));
        bookingService.deleteBooking(booking.getBookingId());
    }

    @Test
    public void givenValidBookingIdShouldDelete() {
        Booking booking = createValidBooking();
        bookingService.deleteBooking(booking.getBookingId());
        Booking saved = bookingRepository.findByBookingId(booking.getBookingId());
        assertNull(saved);
    }

    // ============================
    // = UTILS
    // ============================
    private Booking createValidBooking() {
        return createBooking(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3));
    }

    private Booking createBooking(LocalDate starDate, LocalDate endDate) {
        Booking booking = Booking.builder()
                .id(1l)
                .bookingId(UUID.randomUUID().toString())
                .name("Test name")
                .email("test@gmail.com")
                .date(starDate
                        .datesUntil(endDate.plusDays(1))
                        .collect(Collectors.toSet()))
                .build();

        bookingRepository.save(booking);
        return booking;
    }

    private BookingDTO createBookingDTO(LocalDate starDate, LocalDate endDate) {
        BookingDTO booking = BookingDTO.builder()
                .name("Test name")
                .email("test@gmail.com")
                .startDate(starDate)
                .endDate(endDate)
                .build();

        return booking;
    }

    private ModifyBookingDTO createBookingDTO(String bookingId, LocalDate starDate, LocalDate endDate) {
        ModifyBookingDTO booking = ModifyBookingDTO.builder()
                .bookingId(bookingId)
                .startDate(starDate)
                .endDate(endDate)
                .build();

        return booking;
    }

}
