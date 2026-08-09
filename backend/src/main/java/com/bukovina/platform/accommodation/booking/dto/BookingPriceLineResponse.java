package com.bukovina.platform.accommodation.booking.dto;

import java.math.BigDecimal;

public record BookingPriceLineResponse(
    String code, long quantity, BigDecimal unitAmount, BigDecimal lineTotal) {}
