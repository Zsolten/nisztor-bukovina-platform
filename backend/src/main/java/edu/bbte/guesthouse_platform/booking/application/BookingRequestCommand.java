package edu.bbte.guesthouse_platform.booking.application;

import java.time.LocalDate;

public record BookingRequestCommand(
        String propertySlug,
        String roomSlug,
        String guestName,
        String email,
        LocalDate arrivalDate,
        LocalDate departureDate,
        int guestCount,
        String preferredLanguage,
        String message
) {
}
