package com.bukovina.platform.tourism.activity.dto;

import com.bukovina.platform.tourism.routing.DrivingDistanceMatrixService.CalculationSummary;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AttractionResponse(
    UUID id,
    String slug,
    BigDecimal latitude,
    BigDecimal longitude,
    String googleMapsUrl,
    int recommendedVisitDurationMinutes,
    boolean active,
    List<AttractionTranslation> translations,
    List<String> collectionSlugs,
    CalculationSummary distanceCalculation) {}
