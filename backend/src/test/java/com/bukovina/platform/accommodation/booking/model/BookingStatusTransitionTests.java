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

    booking.transitionTo(BookingStatus.CONFIRMED, changedAt, "ADMIN:test");
    assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    assertEquals(1, booking.getStatusHistory().size());
    assertThrows(
        IllegalStateException.class,
        () -> booking.transitionTo(BookingStatus.REJECTED, changedAt, "ADMIN:test"));

    booking.transitionTo(BookingStatus.CANCELLED, changedAt, "ADMIN:test");
    assertEquals(BookingStatus.CANCELLED, booking.getStatus());
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
            amount("520"), amount("0"), amount("0"), amount("0"), amount("520")),
        "c".repeat(64),
        Instant.parse("2030-02-01T00:00:00Z"));
  }

  private BigDecimal amount(String value) {
    return new BigDecimal(value);
  }
}
