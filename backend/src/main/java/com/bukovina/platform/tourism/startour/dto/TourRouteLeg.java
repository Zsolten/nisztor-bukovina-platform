package com.bukovina.platform.tourism.startour.dto;

public record TourRouteLeg(
    String fromSlug,
    String toSlug,
    Integer distanceMeters,
    Integer durationSeconds,
    String status,
    String failureReason) {}
