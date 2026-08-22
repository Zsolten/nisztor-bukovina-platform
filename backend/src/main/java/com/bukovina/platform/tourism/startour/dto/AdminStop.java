package com.bukovina.platform.tourism.startour.dto;

import java.util.UUID;

public record AdminStop(
    UUID attractionId,
    String slug,
    String name,
    int recommendedVisitDurationMinutes,
    Integer plannedVisitDurationMinutes,
    int effectiveVisitDurationMinutes) {}
