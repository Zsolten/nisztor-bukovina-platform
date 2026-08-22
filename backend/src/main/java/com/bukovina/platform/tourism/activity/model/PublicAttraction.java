package com.bukovina.platform.tourism.activity.model;

import java.math.BigDecimal;
import java.util.UUID;

public record PublicAttraction(
    UUID id,
    String slug,
    String name,
    String shortDescription,
    String detailedDescription,
    String admissionInformation,
    String practicalInformation,
    BigDecimal latitude,
    BigDecimal longitude,
    String googleMapsUrl,
    int recommendedVisitDurationMinutes) {}
