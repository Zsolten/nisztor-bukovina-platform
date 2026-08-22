package com.bukovina.platform.tourism.startour.model;

import java.util.UUID;

public record ValidatedTourStop(UUID attractionId, Integer plannedVisitDurationMinutes) {}
