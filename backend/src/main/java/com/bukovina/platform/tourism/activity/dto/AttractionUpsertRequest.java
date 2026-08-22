package com.bukovina.platform.tourism.activity.dto;

import java.math.BigDecimal;
import java.util.List;

public record AttractionUpsertRequest(
    String slug,
    BigDecimal latitude,
    BigDecimal longitude,
    String googleMapsUrl,
    Integer recommendedVisitDurationMinutes,
    Boolean active,
    List<AttractionTranslation> translations,
    List<String> collectionSlugs) {}
