package com.bukovina.platform.tourism.startour.service;

import com.bukovina.platform.tourism.routing.DrivingRouteProvider;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RouteLeg;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RoutePoint;
import com.bukovina.platform.tourism.routing.StarTourRouteCalculationProperties;
import com.bukovina.platform.tourism.routing.StarTourRouteRateLimiter;
import com.bukovina.platform.tourism.routing.TourismRouteBaseProperties;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
  private final StarTourRouteCalculationProperties properties;
  private final StarTourRouteRateLimiter rateLimiter;
  private final TransactionTemplate transactionTemplate;

  public StarTourRouteService(
      JdbcClient jdbc,
      DrivingRouteProvider provider,
      TourismRouteBaseProperties base,
      StarTourRouteCalculationProperties properties,
      StarTourRouteRateLimiter rateLimiter,
      TransactionTemplate transactionTemplate) {
    this.jdbc = jdbc;
    this.provider = provider;
    this.base = base;
    this.properties = properties;
    this.rateLimiter = rateLimiter;
    this.transactionTemplate = transactionTemplate;
  }

  public StarTourRouteResponse getPublicRoute(
      String tourSlug, List<String> requestedOptionalSlugs, String clientIp) {
    UUID tourId = findPublishedTourId(tourSlug);
    RouteDefinition definition = definition(tourId, requestedOptionalSlugs);
    return calculateIfNeeded(tourId, tourSlug, definition, false, clientIp);
  }

  /** Recalculates the required-stop route after an administrator saves a tour. */
  public RouteStatus recalculateForAdmin(UUID tourId) {
    ensureTourExists(tourId);
    RouteDefinition definition = definition(tourId, List.of());
    if (definition.stops().isEmpty()) {
      return RouteStatus.MISSING;
    }
    return calculateIfNeeded(tourId, null, definition, true, null).routeStatus();
  }

  /** Returns the default-route status without making a Google request. */
  public RouteStatus statusFor(UUID tourId) {
    RouteDefinition definition = definition(tourId, List.of());
    if (definition.stops().isEmpty()) {
      return RouteStatus.MISSING;
    }
    Variant variant = findVariant(tourId, definition.fingerprint());
    if (variant == null) {
      return hasAnyVariant(tourId) ? RouteStatus.STALE : RouteStatus.MISSING;
    }
    return switch (variant.status()) {
      case "PENDING" -> pendingExpired(variant) ? RouteStatus.STALE : RouteStatus.CALCULATING;
      case "FAILED" -> RouteStatus.FAILED;
      case "SUCCESS" ->
          storedLegs(variant.id(), definition.stops().size() + 1) == null
              ? RouteStatus.STALE
              : RouteStatus.READY;
      default -> RouteStatus.STALE;
    };
  }

  public String failureReasonFor(UUID tourId) {
    RouteDefinition definition = definition(tourId, List.of());
    Variant variant = findVariant(tourId, definition.fingerprint());
    return variant != null && "FAILED".equals(variant.status()) ? variant.failureReason() : null;
  }

  private StarTourRouteResponse calculateIfNeeded(
      UUID tourId, String tourSlug, RouteDefinition definition, boolean force, String clientIp) {
    if (definition.stops().isEmpty()) {
      return response(tourSlug, definition, RouteStatus.MISSING, List.of(), true, null, null);
    }
    Variant existing = findVariant(tourId, definition.fingerprint());
    List<RouteLeg> cached =
        existing == null ? null : storedLegs(existing.id(), definition.stops().size() + 1);
    if (!force && existing != null && "SUCCESS".equals(existing.status()) && cached != null) {
      return response(tourSlug, definition, RouteStatus.READY, cached, true, null, null);
    }
    if (!force
        && existing != null
        && "PENDING".equals(existing.status())
        && !pendingExpired(existing)) {
      return response(
          tourSlug,
          definition,
          RouteStatus.CALCULATING,
          List.of(),
          true,
          null,
          existing.calculatedAt());
    }
    if (!force && existing != null && "FAILED".equals(existing.status()) && retryActive(existing)) {
      return response(
          tourSlug,
          definition,
          RouteStatus.FAILED,
          List.of(),
          true,
          existing.failureReason(),
          existing.retryAfter());
    }
    if (!force) {
      rateLimiter.consumeCalculation(clientIp);
    }

    Claim claim = claimCalculation(tourId, definition, force);
    if (claim == Claim.CALCULATING) {
      Variant pending = findVariant(tourId, definition.fingerprint());
      return response(
          tourSlug,
          definition,
          RouteStatus.CALCULATING,
          List.of(),
          true,
          null,
          pending == null ? null : pending.calculatedAt());
    }
    if (claim == Claim.READY) {
      Variant ready = findVariant(tourId, definition.fingerprint());
      List<RouteLeg> legs =
          ready == null ? null : storedLegs(ready.id(), definition.stops().size() + 1);
      if (legs != null) {
        return response(tourSlug, definition, RouteStatus.READY, legs, true, null, null);
      }
      claim = claimCalculation(tourId, definition, true);
      if (claim != Claim.OWNER) {
        return response(tourSlug, definition, RouteStatus.CALCULATING, List.of(), true, null, null);
      }
    }
    if (claim == Claim.FAILED) {
      Variant failed = findVariant(tourId, definition.fingerprint());
      return response(
          tourSlug,
          definition,
          RouteStatus.FAILED,
          List.of(),
          true,
          failed == null ? null : failed.failureReason(),
          failed == null ? null : failed.retryAfter());
    }

    try {
      List<RouteLeg> legs =
          provider.calculate(
              new RoutePoint(base.latitude(), base.longitude()),
              definition.stops().stream().map(TourStop::toRoutePoint).toList(),
              new RoutePoint(base.latitude(), base.longitude()));
      validateLegs(legs, definition.stops().size() + 1);
      storeSuccess(tourId, definition, legs);
      return response(tourSlug, definition, RouteStatus.READY, legs, false, null, null);
    } catch (RuntimeException exception) {
      OffsetDateTime retryAfter = OffsetDateTime.now().plus(properties.failureCooldown());
      String failureReason = reason(exception);
      storeFailure(tourId, definition, failureReason, retryAfter);
      return response(
          tourSlug, definition, RouteStatus.FAILED, List.of(), false, failureReason, retryAfter);
    }
  }

  private Claim claimCalculation(UUID tourId, RouteDefinition definition, boolean force) {
    return transactionTemplate.execute(
        ignored -> {
          Variant variant = findVariant(tourId, definition.fingerprint());
          if (variant == null) {
            if (insertPending(tourId, definition) == 1) {
              return Claim.OWNER;
            }
            variant = findVariant(tourId, definition.fingerprint());
            if (variant == null) {
              throw new IllegalStateException("STAR_TOUR_ROUTE_CLAIM_FAILED");
            }
          }
          if ("PENDING".equals(variant.status()) && !pendingExpired(variant)) {
            return Claim.CALCULATING;
          }
          if (!force && "SUCCESS".equals(variant.status())) {
            return Claim.READY;
          }
          if (!force && "FAILED".equals(variant.status()) && retryActive(variant)) {
            return Claim.FAILED;
          }
          return markPending(variant.id()) == 1 ? Claim.OWNER : Claim.CALCULATING;
        });
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

  private void ensureTourExists(UUID tourId) {
    if (!jdbc.sql("SELECT EXISTS(SELECT 1 FROM star_tour WHERE id = :tourId)")
        .param("tourId", tourId)
        .query(Boolean.class)
        .single()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "STAR_TOUR_NOT_FOUND");
    }
  }

  private RouteDefinition definition(UUID tourId, List<String> requestedOptionalSlugs) {
    List<TourStop> selectedStops = selectStops(stopsFor(tourId), requestedOptionalSlugs);
    if (selectedStops.size() > MAX_INTERMEDIATE_STOPS) {
      throw badRequest("STAR_TOUR_STOP_LIMIT_EXCEEDED");
    }
    return new RouteDefinition(
        selectedStops, selectionKey(selectedStops), fingerprint(selectedStops));
  }

  private List<TourStop> stopsFor(UUID tourId) {
    return jdbc.sql(
            "SELECT attraction.id, attraction.slug, attraction.latitude, attraction.longitude, "
                + "assignment.display_order, assignment.optional_stop "
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
                    rs.getInt("display_order"),
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

  private String fingerprint(List<TourStop> selectedStops) {
    StringBuilder input =
        new StringBuilder("v2|base|")
            .append(decimal(base.latitude()))
            .append('|')
            .append(decimal(base.longitude()));
    for (TourStop stop : selectedStops) {
      input
          .append("|stop|")
          .append(stop.displayOrder())
          .append('|')
          .append(stop.id())
          .append('|')
          .append(decimal(stop.latitude()))
          .append('|')
          .append(decimal(stop.longitude()))
          .append('|')
          .append(stop.optional());
    }
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(input.toString().getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(64);
      for (byte value : digest) {
        hex.append(String.format("%02x", value));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA_256_NOT_AVAILABLE", exception);
    }
  }

  private static String decimal(BigDecimal value) {
    return value.stripTrailingZeros().toPlainString();
  }

  private static String selectionKey(List<TourStop> selectedStops) {
    return selectedStops.stream()
        .filter(TourStop::optional)
        .map(stop -> stop.id().toString())
        .sorted()
        .reduce((first, second) -> first + "," + second)
        .orElse("");
  }

  private Variant findVariant(UUID tourId, String fingerprint) {
    return jdbc.sql(
            "SELECT id, calculation_status, calculated_at, failure_reason, retry_after "
                + "FROM star_tour_route_variant WHERE star_tour_id = :tourId "
                + "AND route_fingerprint = :fingerprint")
        .param("tourId", tourId)
        .param("fingerprint", fingerprint)
        .query(
            (rs, row) ->
                new Variant(
                    rs.getObject("id", UUID.class),
                    rs.getString("calculation_status"),
                    rs.getObject("calculated_at", OffsetDateTime.class),
                    rs.getString("failure_reason"),
                    rs.getObject("retry_after", OffsetDateTime.class)))
        .optional()
        .orElse(null);
  }

  private boolean hasAnyVariant(UUID tourId) {
    return jdbc.sql(
            "SELECT EXISTS(SELECT 1 FROM star_tour_route_variant WHERE star_tour_id = :tourId)")
        .param("tourId", tourId)
        .query(Boolean.class)
        .single();
  }

  private List<RouteLeg> storedLegs(UUID variantId, int expectedCount) {
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
    try {
      validateLegs(legs, expectedCount);
      return legs;
    } catch (RuntimeException exception) {
      return null;
    }
  }

  private int insertPending(UUID tourId, RouteDefinition definition) {
    return jdbc.sql(
            "INSERT INTO star_tour_route_variant (id, star_tour_id, selection_key, route_fingerprint, "
                + "calculation_status, source, calculated_at) VALUES (:id, :tourId, :selectionKey, "
                + ":fingerprint, 'PENDING', :source, :calculatedAt) "
                + "ON CONFLICT (star_tour_id, route_fingerprint) DO NOTHING")
        .param("id", UUID.randomUUID())
        .param("tourId", tourId)
        .param("selectionKey", definition.selectionKey())
        .param("fingerprint", definition.fingerprint())
        .param("source", SOURCE)
        .param("calculatedAt", OffsetDateTime.now())
        .update();
  }

  private int markPending(UUID variantId) {
    return jdbc.sql(
            "UPDATE star_tour_route_variant SET calculation_status = 'PENDING', source = :source, "
                + "calculated_at = :calculatedAt, failure_reason = NULL, retry_after = NULL WHERE id = :id "
                + "AND (calculation_status <> 'PENDING' OR calculated_at < :pendingBefore)")
        .param("source", SOURCE)
        .param("calculatedAt", OffsetDateTime.now())
        .param("pendingBefore", OffsetDateTime.now().minus(properties.pendingTimeout()))
        .param("id", variantId)
        .update();
  }

  private void storeSuccess(UUID tourId, RouteDefinition definition, List<RouteLeg> legs) {
    transactionTemplate.executeWithoutResult(
        ignored -> {
          UUID variantId = variantId(tourId, definition.fingerprint());
          jdbc.sql(
                  "UPDATE star_tour_route_variant SET calculation_status = 'SUCCESS', source = :source, "
                      + "calculated_at = :calculatedAt, failure_reason = NULL, retry_after = NULL WHERE id = :id")
              .param("source", SOURCE)
              .param("calculatedAt", OffsetDateTime.now())
              .param("id", variantId)
              .update();
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

  private void storeFailure(
      UUID tourId, RouteDefinition definition, String failureReason, OffsetDateTime retryAfter) {
    transactionTemplate.executeWithoutResult(
        ignored -> {
          UUID variantId = variantId(tourId, definition.fingerprint());
          jdbc.sql(
                  "UPDATE star_tour_route_variant SET calculation_status = 'FAILED', source = :source, "
                      + "calculated_at = :calculatedAt, failure_reason = :failureReason, retry_after = :retryAfter "
                      + "WHERE id = :id")
              .param("source", SOURCE)
              .param("calculatedAt", OffsetDateTime.now())
              .param("failureReason", failureReason)
              .param("retryAfter", retryAfter)
              .param("id", variantId)
              .update();
          jdbc.sql("DELETE FROM star_tour_route_leg WHERE route_variant_id = :variantId")
              .param("variantId", variantId)
              .update();
        });
  }

  private UUID variantId(UUID tourId, String fingerprint) {
    return jdbc.sql(
            "SELECT id FROM star_tour_route_variant WHERE star_tour_id = :tourId "
                + "AND route_fingerprint = :fingerprint")
        .param("tourId", tourId)
        .param("fingerprint", fingerprint)
        .query(UUID.class)
        .single();
  }

  private boolean pendingExpired(Variant variant) {
    return !variant.calculatedAt().plus(properties.pendingTimeout()).isAfter(OffsetDateTime.now());
  }

  private static boolean retryActive(Variant variant) {
    return variant.retryAfter() != null && variant.retryAfter().isAfter(OffsetDateTime.now());
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
      String tourSlug,
      RouteDefinition definition,
      RouteStatus status,
      List<RouteLeg> legs,
      boolean cached,
      String failureReason,
      OffsetDateTime retryAfter) {
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
        status,
        cached,
        new BaseStop(base.latitude(), base.longitude()),
        java.util.stream.IntStream.range(0, definition.stops().size())
            .mapToObj(
                index -> {
                  TourStop stop = definition.stops().get(index);
                  return new StopResponse(
                      index + 1, stop.slug(), stop.latitude(), stop.longitude(), stop.optional());
                })
            .toList(),
        legResponses,
        legs.stream().mapToInt(RouteLeg::distanceMeters).sum(),
        legs.stream().mapToInt(RouteLeg::durationSeconds).sum(),
        failureReason,
        retryAfter);
  }

  private static String reason(RuntimeException exception) {
    String value = exception.getMessage();
    String normalized = value == null || value.isBlank() ? "GOOGLE_ROUTES_REQUEST_FAILED" : value;
    return normalized.substring(0, Math.min(normalized.length(), 500));
  }

  private static ResponseStatusException badRequest(String reason) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
  }

  public enum RouteStatus {
    READY,
    MISSING,
    STALE,
    CALCULATING,
    FAILED
  }

  public record StarTourRouteResponse(
      String tourSlug,
      RouteStatus routeStatus,
      boolean cached,
      BaseStop base,
      List<StopResponse> stops,
      List<LegResponse> legs,
      int totalDistanceMeters,
      int totalDurationSeconds,
      String failureReason,
      OffsetDateTime retryAfter) {}

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

  private enum Claim {
    OWNER,
    READY,
    CALCULATING,
    FAILED
  }

  private record RouteDefinition(List<TourStop> stops, String selectionKey, String fingerprint) {}

  private record Variant(
      UUID id,
      String status,
      OffsetDateTime calculatedAt,
      String failureReason,
      OffsetDateTime retryAfter) {}

  private record TourStop(
      UUID id,
      String slug,
      BigDecimal latitude,
      BigDecimal longitude,
      int displayOrder,
      boolean optional) {
    private RoutePoint toRoutePoint() {
      return new RoutePoint(latitude, longitude);
    }
  }
}
