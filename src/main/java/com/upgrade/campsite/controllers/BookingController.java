package com.upgrade.campsite.controllers;

import com.upgrade.campsite.domains.booking.BookingService;
import com.upgrade.campsite.dtos.BookingDTO;
import com.upgrade.campsite.dtos.ModifyBookingDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/api/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Operation(summary = "Get available dates for booking.")
    @GetMapping(path = "/availableDates")
    public List<String> getAvailableDates(
            @RequestParam(name = "startDate", required = false)
            @Parameter(name = "startDate", example = "yyyy-MM-dd") @Future LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @Parameter(name = "endDate", example = "yyyy-MM-dd") @Future LocalDate endDate) {

        return bookingService.getAvailableDates(startDate, endDate);
    }

    @Operation(summary = "Retrieve booking.")
    @GetMapping(path = "/{bookingId}")
    public BookingDTO getBooking(@PathVariable(name = "bookingId", required = true) @NotBlank String bookingId) {
        return bookingService.getBooking(bookingId);
    }

    @Operation(summary = "Create new booking.")
    @PostMapping(path = "/new", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookingDTO createNewBooking(@RequestBody @Parameter(name = "Booking") @Valid BookingDTO newBookingDTO) {
        return bookingService.createNewBooking(newBookingDTO);
    }

    @Operation(summary = "Modify existing booking.")
    @PostMapping(path = "/modify", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookingDTO modifyBooking(
            @RequestBody @Parameter(name = "Modify_Booking") @Valid ModifyBookingDTO modifyBookingDTO) {
        return bookingService.modifyBooking(modifyBookingDTO);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "Cancel existing booking by bookingId.")
    @DeleteMapping(path = "/delete/{bookingId}")
    public void deleteBooking(@PathVariable(name = "bookingId", required = true) @NotBlank String bookingId) {
        bookingService.deleteBooking(bookingId);
    }
}
