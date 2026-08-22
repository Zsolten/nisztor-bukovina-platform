package com.bukovina.platform.tourism.startour.model;

import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RoutePoint;
import java.math.BigDecimal;
import java.util.UUID;

public record RouteTourStop(
    UUID id,
    String slug,
    BigDecimal latitude,
    BigDecimal longitude,
    int displayOrder,
    boolean optional) {
  public RoutePoint toRoutePoint() {
    return new RoutePoint(latitude, longitude);
  }
}
