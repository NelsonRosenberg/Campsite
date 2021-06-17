package com.upgrade.campsite.controllers;

import com.upgrade.campsite.AbstractTest;
import com.upgrade.campsite.domains.booking.Booking;
import com.upgrade.campsite.dtos.BookingDTO;
import com.upgrade.campsite.dtos.ModifyBookingDTO;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@AutoConfigureMockMvc
public class BookingControllerTests extends AbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void before() {
        deleteAll();
    }

    // ============================
    // = GET AVAILABLE DATES TESTS
    // ============================
    @Test
    public void givenRequestForNext30DaysShouldReturnAvailableDates() throws Exception {
        mockMvc.perform(get("/api/booking/availableDates")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusMonths(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(31)));
    }

    // ============================
    // = BOOKING TESTS
    // ============================
    @Test
    public void givenInvalidStartDateShouldReturnError() throws Exception {
        BookingDTO bookingDto = createBookingDTO(null, LocalDate.now().plusDays(1));
        mockMvc.perform(post("/api/booking/new")
                .content(toJson(bookingDto))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void givenInvalidEndDateShouldReturnError() throws Exception {
        BookingDTO bookingDto = createBookingDTO(LocalDate.now().plusDays(1), null);
        mockMvc.perform(post("/api/booking/new")
                .content(toJson(bookingDto))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void givenValidBookingShouldBookAndReturnBooking() throws Exception {
        BookingDTO bookingDto = createBookingDTO(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        mockMvc.perform(post("/api/booking/new")
                .content(toJson(bookingDto))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$.bookingId", is(not(empty()))));
    }

    // ============================
    // = MODIFY BOOKING TESTS
    // ============================
    @Test
    public void givenInvalidBookingIdModifyBookingShouldReturnError() throws Exception {
        ModifyBookingDTO modify = ModifyBookingDTO
                .builder()
                .bookingId("123")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(11))
                .build();

        mockMvc.perform(post("/api/booking/modify")
                .content(toJson(modify))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void givenInvalidModifyBookingShouldReturnError() throws Exception {
        Booking existing = createValidBooking();

        ModifyBookingDTO modify = ModifyBookingDTO
                .builder()
                .bookingId(existing.getBookingId())
                .startDate(LocalDate.now().plusDays(10))
                .endDate(null)
                .build();

        mockMvc.perform(post("/api/booking/modify")
                .content(toJson(modify))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void givenValidModifyBookingShouldModifyAndReturnBooking() throws Exception {
        Booking existing = createValidBooking();

        ModifyBookingDTO modify = ModifyBookingDTO
                .builder()
                .bookingId(existing.getBookingId())
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(11))
                .build();

        mockMvc.perform(post("/api/booking/modify")
                .content(toJson(modify))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$.bookingId", is(not(empty()))));
    }

    // ============================
    // = DELETE BOOKING TESTS
    // ============================
    @Test
    public void givenEmptyBookingIdShouldReturnError() throws Exception {
        mockMvc.perform(delete("/api/booking/delete"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void givenInvalidBookingIdShouldReturnError() throws Exception {
        mockMvc.perform(delete("/api/booking/delete/123"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void givenValidBookingIdShouldReturnOk() throws Exception {
        Booking booking = createValidBooking();
        mockMvc.perform(delete(String.format("/api/booking/delete/%s", booking.getBookingId())))
                .andExpect(status().isOk());
    }
}
