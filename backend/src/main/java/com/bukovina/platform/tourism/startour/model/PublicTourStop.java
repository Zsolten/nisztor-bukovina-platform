package com.bukovina.platform.tourism.startour.model;

import java.math.BigDecimal;

public record PublicTourStop(
    String slug,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    String googleMapsUrl,
    boolean optional,
    int visitDurationMinutes) {}
