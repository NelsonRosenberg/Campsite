package com.upgrade.campsite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upgrade.campsite.domains.booking.Booking;
import com.upgrade.campsite.domains.booking.BookingRepository;
import com.upgrade.campsite.domains.booking.BookingService;
import com.upgrade.campsite.domains.booking.BookingCacheService;
import com.upgrade.campsite.dtos.BookingDTO;
import com.upgrade.campsite.dtos.ModifyBookingDTO;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class})
public abstract class AbstractTest {

    @Autowired
    public BookingService bookingService;

    @Autowired
    public BookingCacheService cachingService;

    @Autowired
    public BookingRepository bookingRepository;

    // ============================
    // = UTILS
    // ============================
    public Booking createValidBooking() {
        return createBooking(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3));
    }

    public Booking createBooking(LocalDate starDate, LocalDate endDate) {
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

    public BookingDTO createBookingDTO(LocalDate starDate, LocalDate endDate) {
        BookingDTO booking = BookingDTO.builder()
                .name("Test name")
                .email("test@gmail.com")
                .startDate(starDate)
                .endDate(endDate)
                .build();

        return booking;
    }

    public ModifyBookingDTO createBookingDTO(String bookingId, LocalDate starDate, LocalDate endDate) {
        ModifyBookingDTO booking = ModifyBookingDTO.builder()
                .bookingId(bookingId)
                .startDate(starDate)
                .endDate(endDate)
                .build();

        return booking;
    }

    public String toJson(Object booking) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.writeValueAsString(booking);
    }

    @Transactional
    public void deleteAll() {
        bookingRepository.deleteAll();
        cachingService.clearCache();
    }

}
