package com.bukovina.platform.accommodation.booking.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BookingStatusTransitionTests {

  @Test
  void enforcesDomainTransitionsAndRecordsSuccessfulChanges() {
    BookingRequest booking = booking();
    Instant changedAt = Instant.parse("2030-01-01T10:00:00Z");

    assertThrows(
        InvalidBookingStatusTransitionException.class,
        () -> booking.transitionTo(BookingStatus.CONFIRMED, changedAt, "ADMIN:test"));
    assertEquals(0, booking.getStatusHistory().size());

    booking.transitionTo(BookingStatus.UNDER_REVIEW, changedAt, "ADMIN:test");
    assertEquals(BookingStatus.UNDER_REVIEW, booking.getStatus());
    assertEquals(1, booking.getStatusHistory().size());

    booking.transitionTo(BookingStatus.CONFIRMED, changedAt.plusSeconds(1), "ADMIN:test");
    assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    assertEquals(2, booking.getStatusHistory().size());
    assertThrows(
        InvalidBookingStatusTransitionException.class,
        () -> booking.transitionTo(BookingStatus.REJECTED, changedAt, "ADMIN:test"));
    assertEquals(2, booking.getStatusHistory().size());

    booking.transitionTo(BookingStatus.CANCELLED, changedAt.plusSeconds(2), "ADMIN:test");
    assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    assertEquals(3, booking.getStatusHistory().size());
  }

  private BookingRequest booking() {
    return new BookingRequest(
        UUID.randomUUID(),
        "NB-0123456789ABCDEF",
        "a".repeat(64),
        "b".repeat(64),
        LocalDate.of(2030, 1, 10),
        LocalDate.of(2030, 1, 12),
        2,
        0,
        0,
        0,
        0,
        "Test Guest",
        "test@example.com",
        "+40 700 000 000",
        "en",
        null,
        new BookingPriceBreakdown(
            amount("520"),
            amount("520"),
            amount("0"),
            amount("0"),
            amount("0"),
            amount("0"),
            amount("520")),
        "c".repeat(64),
        Instant.parse("2030-02-01T00:00:00Z"));
  }

  private BigDecimal amount(String value) {
    return new BigDecimal(value);
  }
}
