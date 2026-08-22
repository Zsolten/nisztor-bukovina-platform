package com.bukovina.platform.tourism.startour.dto;

import com.bukovina.platform.tourism.startour.model.RouteStatus;
import java.util.List;
import java.util.UUID;

public record StarTourResponse(
    UUID id,
    String slug,
    String mapColor,
    boolean published,
    boolean active,
    List<StarTourTranslation> translations,
    List<String> tags,
    List<StarTourImage> images,
    TourTotals totals,
    RouteStatus routeStatus,
    String routeFailureReason) {}
