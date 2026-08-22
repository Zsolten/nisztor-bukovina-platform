package com.bukovina.platform.tourism.startour.dto;

import com.bukovina.platform.tourism.startour.model.RouteStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record StarTourRouteResponse(
    String tourSlug,
    RouteStatus routeStatus,
    boolean cached,
    RouteBaseStop base,
    List<RouteStopResponse> stops,
    List<RouteLegResponse> legs,
    int totalDistanceMeters,
    int totalDurationSeconds,
    String failureReason,
    OffsetDateTime retryAfter) {}
