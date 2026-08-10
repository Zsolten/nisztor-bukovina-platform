package com.bukovina.platform.accommodation.booking.dto;

import java.util.List;

public record BookingQuoteResponse(
    String currency,
    long nights,
    int totalGuests,
    int selectedRoomCount,
    int selectedCapacity,
    List<BookingPriceLineResponse> lines,
    BookingPriceBreakdownResponse priceBreakdown,
    boolean requestOnly) {}
