package com.bukovina.platform.tourism.startour.service;

import com.bukovina.platform.tourism.routing.DrivingRouteProvider;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RouteLeg;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RoutePoint;
import com.bukovina.platform.tourism.routing.TourismRouteBaseProperties;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StarTourRouteService {

  private static final int MAX_INTERMEDIATE_STOPS = 10;
  private static final String SOURCE = "GOOGLE_ROUTES";
  private final JdbcClient jdbc;
  private final DrivingRouteProvider provider;
  private final TourismRouteBaseProperties base;
  private final TransactionTemplate transactionTemplate;

  public StarTourRouteService(
      JdbcClient jdbc,
      DrivingRouteProvider provider,
      TourismRouteBaseProperties base,
      TransactionTemplate transactionTemplate) {
    this.jdbc = jdbc;
    this.provider = provider;
    this.base = base;
    this.transactionTemplate = transactionTemplate;
  }

  /**
   * Returns the cached round trip for a selected optional-stop combination, calculating it once if
   * necessary. The guesthouse is always both the origin and destination.
   */
  public StarTourRouteResponse getPublicRoute(
      String tourSlug, List<String> requestedOptionalSlugs) {
    UUID tourId = findPublishedTourId(tourSlug);
    List<TourStop> availableStops = stopsFor(tourId);
    List<TourStop> selectedStops = selectStops(availableStops, requestedOptionalSlugs);
    if (selectedStops.size() > MAX_INTERMEDIATE_STOPS) {
      throw badRequest("STAR_TOUR_STOP_LIMIT_EXCEEDED");
    }

    String selectionKey = selectionKey(selectedStops);
    StoredRoute cached = findStored(tourId, selectionKey);
    if (cached != null) {
      return response(tourSlug, selectedStops, cached.legs(), true);
    }

    List<RouteLeg> legs;
    try {
      legs =
          provider.calculate(
              new RoutePoint(base.latitude(), base.longitude()),
              selectedStops.stream().map(TourStop::toRoutePoint).toList(),
              new RoutePoint(base.latitude(), base.longitude()));
      validateLegs(legs, selectedStops.size() + 1);
    } catch (RuntimeException exception) {
      storeFailure(tourId, selectionKey, reason(exception));
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "STAR_TOUR_ROUTE_CALCULATION_FAILED", exception);
    }

    storeSuccess(tourId, selectionKey, legs);
    return response(tourSlug, selectedStops, legs, false);
  }

  private UUID findPublishedTourId(String slug) {
    return jdbc.sql(
            "SELECT id FROM star_tour WHERE slug = :slug AND published = TRUE AND active = TRUE")
        .param("slug", slug)
        .query(UUID.class)
        .optional()
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "STAR_TOUR_NOT_FOUND"));
  }

  private List<TourStop> stopsFor(UUID tourId) {
    return jdbc.sql(
            "SELECT attraction.id, attraction.slug, attraction.latitude, attraction.longitude, "
                + "assignment.optional_stop "
                + "FROM star_tour_attraction assignment JOIN attraction ON attraction.id = assignment.attraction_id "
                + "WHERE assignment.star_tour_id = :tourId AND attraction.active = TRUE "
                + "ORDER BY assignment.display_order")
        .param("tourId", tourId)
        .query(
            (rs, row) ->
                new TourStop(
                    rs.getObject("id", UUID.class),
                    rs.getString("slug"),
                    rs.getBigDecimal("latitude"),
                    rs.getBigDecimal("longitude"),
                    rs.getBoolean("optional_stop")))
        .list();
  }

  private static List<TourStop> selectStops(
      List<TourStop> availableStops, List<String> requestedOptionalSlugs) {
    List<String> requested = requestedOptionalSlugs == null ? List.of() : requestedOptionalSlugs;
    if (requested.stream().anyMatch(slug -> slug == null || slug.isBlank())) {
      throw badRequest("INVALID_OPTIONAL_STOPS");
    }
    Set<String> selection = new HashSet<>(requested);
    if (selection.size() != requested.size()) {
      throw badRequest("INVALID_OPTIONAL_STOPS");
    }
    Set<String> availableOptionalSlugs =
        availableStops.stream()
            .filter(TourStop::optional)
            .map(TourStop::slug)
            .collect(java.util.stream.Collectors.toSet());
    if (!availableOptionalSlugs.containsAll(selection)) {
      throw badRequest("INVALID_OPTIONAL_STOPS");
    }
    return availableStops.stream()
        .filter(stop -> !stop.optional() || selection.contains(stop.slug()))
        .toList();
  }

  private static String selectionKey(List<TourStop> selectedStops) {
    return selectedStops.stream()
        .filter(TourStop::optional)
        .map(stop -> stop.id().toString())
        .sorted()
        .reduce((first, second) -> first + "," + second)
        .orElse("");
  }

  private StoredRoute findStored(UUID tourId, String selectionKey) {
    UUID variantId =
        jdbc.sql(
                "SELECT id FROM star_tour_route_variant WHERE star_tour_id = :tourId "
                    + "AND selection_key = :selectionKey AND calculation_status = 'SUCCESS'")
            .param("tourId", tourId)
            .param("selectionKey", selectionKey)
            .query(UUID.class)
            .optional()
            .orElse(null);
    if (variantId == null) {
      return null;
    }
    List<RouteLeg> legs =
        jdbc.sql(
                "SELECT distance_meters, duration_seconds, encoded_polyline FROM star_tour_route_leg "
                    + "WHERE route_variant_id = :variantId ORDER BY leg_order")
            .param("variantId", variantId)
            .query(
                (rs, row) ->
                    new RouteLeg(
                        rs.getInt("distance_meters"),
                        rs.getInt("duration_seconds"),
                        rs.getString("encoded_polyline")))
            .list();
    return new StoredRoute(legs);
  }

  private void storeSuccess(UUID tourId, String selectionKey, List<RouteLeg> legs) {
    transactionTemplate.executeWithoutResult(
        ignored -> {
          UUID variantId = upsertVariant(tourId, selectionKey, "SUCCESS", null);
          jdbc.sql("DELETE FROM star_tour_route_leg WHERE route_variant_id = :variantId")
              .param("variantId", variantId)
              .update();
          for (int index = 0; index < legs.size(); index++) {
            RouteLeg leg = legs.get(index);
            jdbc.sql(
                    "INSERT INTO star_tour_route_leg (route_variant_id, leg_order, from_stop_index, "
                        + "to_stop_index, distance_meters, duration_seconds, encoded_polyline) "
                        + "VALUES (:variantId, :legOrder, :fromStopIndex, :toStopIndex, :distance, :duration, :polyline)")
                .param("variantId", variantId)
                .param("legOrder", index)
                .param("fromStopIndex", index)
                .param("toStopIndex", index + 1)
                .param("distance", leg.distanceMeters())
                .param("duration", leg.durationSeconds())
                .param("polyline", leg.encodedPolyline())
                .update();
          }
        });
  }

  private void storeFailure(UUID tourId, String selectionKey, String failureReason) {
    transactionTemplate.executeWithoutResult(
        ignored -> {
          UUID variantId = upsertVariant(tourId, selectionKey, "FAILED", failureReason);
          jdbc.sql("DELETE FROM star_tour_route_leg WHERE route_variant_id = :variantId")
              .param("variantId", variantId)
              .update();
        });
  }

  private UUID upsertVariant(
      UUID tourId, String selectionKey, String status, String failureReason) {
    return jdbc.sql(
            "INSERT INTO star_tour_route_variant (id, star_tour_id, selection_key, calculation_status, "
                + "source, calculated_at, failure_reason) VALUES (:id, :tourId, :selectionKey, :status, "
                + ":source, :calculatedAt, :failureReason) ON CONFLICT (star_tour_id, selection_key) "
                + "DO UPDATE SET calculation_status = EXCLUDED.calculation_status, source = EXCLUDED.source, "
                + "calculated_at = EXCLUDED.calculated_at, failure_reason = EXCLUDED.failure_reason "
                + "RETURNING id")
        .param("id", UUID.randomUUID())
        .param("tourId", tourId)
        .param("selectionKey", selectionKey)
        .param("status", status)
        .param("source", SOURCE)
        .param("calculatedAt", OffsetDateTime.now())
        .param("failureReason", failureReason)
        .query(UUID.class)
        .single();
  }

  private static void validateLegs(List<RouteLeg> legs, int expectedCount) {
    if (legs == null
        || legs.size() != expectedCount
        || legs.stream()
            .anyMatch(
                leg ->
                    leg.distanceMeters() < 0
                        || leg.durationSeconds() < 0
                        || leg.encodedPolyline() == null
                        || leg.encodedPolyline().isBlank())) {
      throw new IllegalStateException("GOOGLE_ROUTES_INVALID_ROUTE_LEGS");
    }
  }

  private StarTourRouteResponse response(
      String tourSlug, List<TourStop> selectedStops, List<RouteLeg> legs, boolean cached) {
    List<LegResponse> legResponses =
        java.util.stream.IntStream.range(0, legs.size())
            .mapToObj(
                index -> {
                  RouteLeg leg = legs.get(index);
                  return new LegResponse(
                      index,
                      index,
                      index + 1,
                      leg.distanceMeters(),
                      leg.durationSeconds(),
                      leg.encodedPolyline());
                })
            .toList();
    return new StarTourRouteResponse(
        tourSlug,
        cached,
        new BaseStop(base.latitude(), base.longitude()),
        java.util.stream.IntStream.range(0, selectedStops.size())
            .mapToObj(
                index -> {
                  TourStop stop = selectedStops.get(index);
                  return new StopResponse(
                      index + 1, stop.slug(), stop.latitude(), stop.longitude(), stop.optional());
                })
            .toList(),
        legResponses,
        legs.stream().mapToInt(RouteLeg::distanceMeters).sum(),
        legs.stream().mapToInt(RouteLeg::durationSeconds).sum());
  }

  private static String reason(RuntimeException exception) {
    String value = exception.getMessage();
    String normalized = value == null || value.isBlank() ? "GOOGLE_ROUTES_REQUEST_FAILED" : value;
    return normalized.substring(0, Math.min(normalized.length(), 500));
  }

  private static ResponseStatusException badRequest(String reason) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
  }

  public record StarTourRouteResponse(
      String tourSlug,
      boolean cached,
      BaseStop base,
      List<StopResponse> stops,
      List<LegResponse> legs,
      int totalDistanceMeters,
      int totalDurationSeconds) {}

  public record BaseStop(BigDecimal latitude, BigDecimal longitude) {}

  public record StopResponse(
      int waypointIndex,
      String slug,
      BigDecimal latitude,
      BigDecimal longitude,
      boolean optional) {}

  public record LegResponse(
      int order,
      int fromStopIndex,
      int toStopIndex,
      int distanceMeters,
      int durationSeconds,
      String encodedPolyline) {}

  private record StoredRoute(List<RouteLeg> legs) {}

  private record TourStop(
      UUID id, String slug, BigDecimal latitude, BigDecimal longitude, boolean optional) {
    private RoutePoint toRoutePoint() {
      return new RoutePoint(latitude, longitude);
    }
  }
}
