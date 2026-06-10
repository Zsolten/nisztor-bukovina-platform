package edu.bbte.guesthouse_platform.booking.application;

import edu.bbte.guesthouse_platform.booking.domain.BookingStatus;

import java.util.UUID;

public record BookingRequestResult(UUID requestId, BookingStatus status) {
}
