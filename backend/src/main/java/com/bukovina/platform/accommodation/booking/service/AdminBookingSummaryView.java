package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AdminBookingSummaryView(
    UUID id,
    String publicReference,
    UUID guesthouseId,
    String guesthouseName,
    BookingStatus status,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    int adults,
    int childrenAge3to10,
    int childrenAge0to3,
    String contactName,
    BigDecimal totalPayable,
    String currency,
    Instant createdAt) {}
