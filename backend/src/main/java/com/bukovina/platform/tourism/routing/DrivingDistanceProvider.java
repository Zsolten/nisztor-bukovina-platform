package com.bukovina.platform.tourism.routing;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Calculates driving distances for attraction pairs; it is never called from public reads. */
public interface DrivingDistanceProvider {

  List<MatrixElement> calculate(List<AttractionPoint> origins, List<AttractionPoint> destinations);

  record AttractionPoint(UUID id, BigDecimal latitude, BigDecimal longitude) {}

  record MatrixElement(
      int originIndex,
      int destinationIndex,
      Integer distanceMeters,
      Integer durationSeconds,
      String failureReason) {}
}
