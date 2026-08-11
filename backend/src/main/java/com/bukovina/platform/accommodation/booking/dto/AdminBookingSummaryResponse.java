package com.bukovina.platform.accommodation.booking.dto;

import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import com.bukovina.platform.accommodation.booking.service.AdminBookingSummaryView;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record AdminBookingSummaryResponse(
    UUID id,
    String publicReference,
    UUID guesthouseId,
    String guesthouseName,
    BookingStatus status,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    long nights,
    int totalGuests,
    String contactName,
    BigDecimal totalPayable,
    String currency,
    Instant createdAt) {

  public static AdminBookingSummaryResponse from(AdminBookingSummaryView booking) {
    return new AdminBookingSummaryResponse(
        booking.id(),
        booking.publicReference(),
        booking.guesthouseId(),
        booking.guesthouseName(),
        booking.status(),
        booking.checkInDate(),
        booking.checkOutDate(),
        ChronoUnit.DAYS.between(booking.checkInDate(), booking.checkOutDate()),
        booking.adults() + booking.childrenAge3to10() + booking.childrenAge0to3(),
        booking.contactName(),
        booking.totalPayable(),
        booking.currency(),
        booking.createdAt());
  }
}
