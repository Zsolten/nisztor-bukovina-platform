package com.bukovina.platform.tourism.routing;

import com.bukovina.platform.tourism.routing.DrivingDistanceProvider.AttractionPoint;
import com.bukovina.platform.tourism.routing.DrivingDistanceProvider.MatrixElement;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class DrivingDistanceMatrixService {

  private static final String SOURCE = "GOOGLE_ROUTES";
  private final JdbcClient jdbc;
  private final DrivingDistanceProvider provider;

  public DrivingDistanceMatrixService(JdbcClient jdbc, DrivingDistanceProvider provider) {
    this.jdbc = jdbc;
    this.provider = provider;
  }

  /** Recalculates only rows that contain the changed attraction. */
  public CalculationSummary recalculateAffectedPairs(UUID attractionId) {
    List<AttractionPoint> points =
        jdbc.sql("SELECT id, latitude, longitude FROM attraction ORDER BY id")
            .query(
                (rs, row) ->
                    new AttractionPoint(
                        rs.getObject("id", UUID.class),
                        rs.getBigDecimal("latitude"),
                        rs.getBigDecimal("longitude")))
            .list();
    AttractionPoint affected =
        points.stream()
            .filter(point -> point.id().equals(attractionId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("ATTRACTION_NOT_FOUND"));
    List<AttractionPoint> others =
        points.stream().filter(point -> !point.id().equals(attractionId)).toList();
    if (others.isEmpty()) {
      return new CalculationSummary(0, 0, 0);
    }

    jdbc.sql(
            "DELETE FROM attraction_driving_distance "
                + "WHERE attraction_a_id = :id OR attraction_b_id = :id")
        .param("id", attractionId)
        .update();
    insertPending(affected, others);

    int successful = calculateAndStore(List.of(affected), others);
    int total = others.size();
    return new CalculationSummary(total, successful, total - successful);
  }

  private void insertPending(AttractionPoint affected, List<AttractionPoint> others) {
    for (AttractionPoint other : others) {
      PairOfAttractions pair = PairOfAttractions.of(affected, other);
      jdbc.sql(
              "INSERT INTO attraction_driving_distance "
                  + "(attraction_a_id, attraction_b_id, calculation_status, source) "
                  + "VALUES (:first, :second, 'PENDING', :source)")
          .param("first", pair.first().id())
          .param("second", pair.second().id())
          .param("source", SOURCE)
          .update();
    }
  }

  private int calculateAndStore(List<AttractionPoint> origins, List<AttractionPoint> destinations) {
    try {
      Map<Pair, MatrixElement> results = index(provider.calculate(origins, destinations));
      int successful = 0;
      for (int originIndex = 0; originIndex < origins.size(); originIndex++) {
        for (int destinationIndex = 0; destinationIndex < destinations.size(); destinationIndex++) {
          MatrixElement result = results.get(new Pair(originIndex, destinationIndex));
          if (result != null
              && result.failureReason() == null
              && result.distanceMeters() != null
              && result.durationSeconds() != null) {
            markSuccess(origins.get(originIndex), destinations.get(destinationIndex), result);
            successful++;
          } else {
            markFailed(
                origins.get(originIndex),
                destinations.get(destinationIndex),
                result == null ? "GOOGLE_ROUTES_MISSING_MATRIX_ELEMENT" : result.failureReason());
          }
        }
      }
      return successful;
    } catch (RuntimeException exception) {
      String reason =
          exception.getMessage() == null ? "GOOGLE_ROUTES_REQUEST_FAILED" : exception.getMessage();
      for (AttractionPoint origin : origins) {
        for (AttractionPoint destination : destinations) {
          markFailed(origin, destination, reason);
        }
      }
      return 0;
    }
  }

  private static Map<Pair, MatrixElement> index(List<MatrixElement> elements) {
    Map<Pair, MatrixElement> result = new HashMap<>();
    for (MatrixElement element : elements) {
      result.put(new Pair(element.originIndex(), element.destinationIndex()), element);
    }
    return result;
  }

  private void markSuccess(
      AttractionPoint origin, AttractionPoint destination, MatrixElement result) {
    PairOfAttractions pair = PairOfAttractions.of(origin, destination);
    jdbc.sql(
            "UPDATE attraction_driving_distance SET distance_meters = :distance, duration_seconds = :duration, "
                + "calculation_status = 'SUCCESS', source = :source, calculated_at = :calculatedAt, "
                + "failure_reason = NULL WHERE attraction_a_id = :first AND attraction_b_id = :second")
        .param("distance", result.distanceMeters())
        .param("duration", result.durationSeconds())
        .param("source", SOURCE)
        .param("calculatedAt", OffsetDateTime.now())
        .param("first", pair.first().id())
        .param("second", pair.second().id())
        .update();
  }

  private void markFailed(AttractionPoint origin, AttractionPoint destination, String reason) {
    PairOfAttractions pair = PairOfAttractions.of(origin, destination);
    jdbc.sql(
            "UPDATE attraction_driving_distance SET calculation_status = 'FAILED', source = :source, "
                + "calculated_at = :calculatedAt, failure_reason = :reason "
                + "WHERE attraction_a_id = :first AND attraction_b_id = :second")
        .param("source", SOURCE)
        .param("calculatedAt", OffsetDateTime.now())
        .param("reason", limit(reason))
        .param("first", pair.first().id())
        .param("second", pair.second().id())
        .update();
  }

  private static String limit(String value) {
    String normalized = value == null || value.isBlank() ? "GOOGLE_ROUTES_REQUEST_FAILED" : value;
    return normalized.substring(0, Math.min(normalized.length(), 500));
  }

  public record CalculationSummary(int total, int successful, int failed) {}

  private record Pair(int originIndex, int destinationIndex) {}

  private record PairOfAttractions(AttractionPoint first, AttractionPoint second) {
    private static PairOfAttractions of(AttractionPoint one, AttractionPoint other) {
      return one.id().toString().compareTo(other.id().toString()) < 0
          ? new PairOfAttractions(one, other)
          : new PairOfAttractions(other, one);
    }
  }
}
