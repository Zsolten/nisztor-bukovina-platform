package com.bukovina.platform.accommodation.booking.dto;

import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import java.math.BigDecimal;

public record BookingRequestCreatedResponse(
    String reference,
    BookingStatus status,
    String currency,
    long nights,
    int totalGuests,
    BigDecimal totalPayable,
    boolean requestOnly) {}
