package com.bukovina.platform.accommodation.booking.dto;

import java.math.BigDecimal;

public record BookingPriceBreakdownResponse(
    BigDecimal accommodationTotal,
    BigDecimal adultAccommodationTotal,
    BigDecimal childAccommodationTotal,
    BigDecimal singleRoomSurcharge,
    BigDecimal breakfastTotal,
    BigDecimal dinnerTotal,
    BigDecimal totalPayable) {}
