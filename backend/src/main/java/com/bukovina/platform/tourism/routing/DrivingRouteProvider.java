package com.bukovina.platform.tourism.routing;

import java.math.BigDecimal;
import java.util.List;

/** Retrieves the actual driving geometry for one ordered round trip. */
public interface DrivingRouteProvider {

  List<RouteLeg> calculate(
      RoutePoint origin, List<RoutePoint> intermediates, RoutePoint destination);

  record RoutePoint(BigDecimal latitude, BigDecimal longitude) {}

  record RouteLeg(int distanceMeters, int durationSeconds, String encodedPolyline) {}
}
