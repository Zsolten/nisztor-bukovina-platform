package com.bukovina.platform.tourism.activity.dto;

import java.math.BigDecimal;
import java.util.List;

public record AttractionPublicResponse(
    String slug,
    String name,
    String shortDescription,
    String detailedDescription,
    String admissionInformation,
    String practicalInformation,
    BigDecimal latitude,
    BigDecimal longitude,
    String googleMapsUrl,
    int recommendedVisitDurationMinutes,
    List<String> collectionSlugs) {}
