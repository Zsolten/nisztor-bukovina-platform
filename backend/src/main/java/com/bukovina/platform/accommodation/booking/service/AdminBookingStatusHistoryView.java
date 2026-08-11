package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import java.time.Instant;

public record AdminBookingStatusHistoryView(
    BookingStatus status, Instant changedAt, String changedBy) {}
