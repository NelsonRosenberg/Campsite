package com.upgrade.campsite.constants;

public class ErrorMessages {

    public static final String BOOKING_ID_NOT_FOUND = "Could not find the requested booking.";

    public static final String INVALID_DATE_START_BEFORE_END = "Dates are invalid. Start date must be after end date.";

    public static final String INVALID_BOOKING_DATES = "Invalid booking dates. Reservation is for a maximum of 3 days " +
            "and must be made with a minimum of 1 day or a maximum of 30 days in advance.";

    public static final String ALREADY_BOOKED = "Apologies, but the date/s are already taken.";

    public static final String USER_FRIENDLY_GENERAL_ERROR = "Apologies, but we could not process your request at the moment. Please try again later.";
}
