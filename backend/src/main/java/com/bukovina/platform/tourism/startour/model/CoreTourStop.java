package com.bukovina.platform.tourism.startour.model;

import java.util.UUID;

public record CoreTourStop(
    UUID attractionId,
    String slug,
    String name,
    int recommendedVisitDurationMinutes,
    Integer plannedVisitDurationMinutes) {
  public int effectiveVisitDurationMinutes() {
    return plannedVisitDurationMinutes == null
        ? recommendedVisitDurationMinutes
        : plannedVisitDurationMinutes;
  }
}
