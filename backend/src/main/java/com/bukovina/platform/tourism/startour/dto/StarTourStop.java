package com.bukovina.platform.tourism.startour.dto;

import java.math.BigDecimal;

public record StarTourStop(
    String slug,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    String googleMapsUrl,
    boolean optional,
    int visitDurationMinutes) {}
