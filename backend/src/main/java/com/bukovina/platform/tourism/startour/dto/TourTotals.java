package com.bukovina.platform.tourism.startour.dto;

import java.util.List;

public record TourTotals(
    Integer travelDistanceMeters,
    Integer travelDurationSeconds,
    int visitDurationMinutes,
    Integer totalDurationSeconds,
    boolean routeDataComplete,
    List<TourRouteLeg> routeLegs) {}
