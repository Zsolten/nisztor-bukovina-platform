package com.bukovina.platform.tourism.activity.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Attraction(
    UUID id,
    String slug,
    BigDecimal latitude,
    BigDecimal longitude,
    String googleMapsUrl,
    int recommendedVisitDurationMinutes,
    boolean active) {}
