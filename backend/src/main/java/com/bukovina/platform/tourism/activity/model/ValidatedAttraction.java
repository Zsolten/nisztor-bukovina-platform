package com.bukovina.platform.tourism.activity.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ValidatedAttraction(
    String slug,
    BigDecimal latitude,
    BigDecimal longitude,
    String googleMapsUrl,
    int recommendedVisitDurationMinutes,
    boolean active,
    Map<String, AttractionContent> translations,
    List<String> collectionSlugs) {}
