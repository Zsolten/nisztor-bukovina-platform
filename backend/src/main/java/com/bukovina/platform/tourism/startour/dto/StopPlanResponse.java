package com.bukovina.platform.tourism.startour.dto;

import java.util.List;
import java.util.UUID;

public record StopPlanResponse(
    UUID tourId,
    boolean published,
    List<AdminStop> stops,
    List<UUID> assignedAttractionIds,
    TourTotals totals) {}
