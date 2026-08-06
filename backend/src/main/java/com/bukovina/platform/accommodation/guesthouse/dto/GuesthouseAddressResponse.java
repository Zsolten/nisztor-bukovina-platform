package com.bukovina.platform.accommodation.guesthouse.dto;

import java.math.BigDecimal;

public record GuesthouseAddressResponse(
    String formatted, BigDecimal latitude, BigDecimal longitude) {}
