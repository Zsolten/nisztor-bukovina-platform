package com.bukovina.platform.tourism.startour.dto;

public record RouteLegResponse(
    int order,
    int fromStopIndex,
    int toStopIndex,
    int distanceMeters,
    int durationSeconds,
    String encodedPolyline) {}
