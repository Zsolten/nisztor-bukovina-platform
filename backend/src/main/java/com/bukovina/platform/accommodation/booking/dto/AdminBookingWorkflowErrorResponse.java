package com.bukovina.platform.accommodation.booking.dto;

import com.bukovina.platform.accommodation.booking.model.BookingStatus;

public record AdminBookingWorkflowErrorResponse(
    String code, BookingStatus currentStatus, BookingStatus requestedStatus) {}
