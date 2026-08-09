package com.bukovina.platform.accommodation.booking.dto;

import java.util.List;

public record BookingErrorResponse(
    String code, List<BookingFieldErrorResponse> errors, BookingQuoteResponse currentQuote) {}
