package com.bukovina.platform.tourism.startour.dto;

import com.bukovina.platform.tourism.startour.model.RouteStatus;
import java.util.List;

public record StarTourPublicResponse(
    String slug,
    String name,
    String shortDescription,
    String detailedDescription,
    String mapColor,
    List<String> tags,
    List<StarTourImage> images,
    List<StarTourStop> stops,
    TourTotals totals,
    RouteStatus routeStatus) {}
