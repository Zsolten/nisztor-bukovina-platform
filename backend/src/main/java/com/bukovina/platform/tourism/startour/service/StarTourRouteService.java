package com.bukovina.platform.tourism.startour.service;

import com.bukovina.platform.tourism.routing.DrivingRouteProvider;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RouteLeg;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RoutePoint;
import com.bukovina.platform.tourism.routing.StarTourRouteCalculationProperties;
import com.bukovina.platform.tourism.routing.StarTourRouteRateLimiter;
import com.bukovina.platform.tourism.routing.TourismRouteBaseProperties;
import com.bukovina.platform.tourism.startour.dao.StarTourRouteDao;
import com.bukovina.platform.tourism.startour.dto.RouteBaseStop;
import com.bukovina.platform.tourism.startour.dto.RouteLegResponse;
import com.bukovina.platform.tourism.startour.dto.RouteStopResponse;
import com.bukovina.platform.tourism.startour.dto.StarTourRouteResponse;
import com.bukovina.platform.tourism.startour.exception.StarTourException;
import com.bukovina.platform.tourism.startour.model.PublishedStarTour;
import com.bukovina.platform.tourism.startour.model.RouteDefinition;
import com.bukovina.platform.tourism.startour.model.RouteStatus;
import com.bukovina.platform.tourism.startour.model.RouteTourStop;
import com.bukovina.platform.tourism.startour.model.RouteVariant;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class StarTourRouteService {
  private static final int MAX_INTERMEDIATE_STOPS = 10;
  private final StarTourRouteDao dao;
  private final DrivingRouteProvider provider;
  private final TourismRouteBaseProperties base;
  private final StarTourRouteCalculationProperties properties;
  private final StarTourRouteRateLimiter rateLimiter;
  private final TransactionTemplate transactionTemplate;

  public StarTourRouteService(
      StarTourRouteDao dao,
      DrivingRouteProvider provider,
      TourismRouteBaseProperties base,
      StarTourRouteCalculationProperties properties,
      StarTourRouteRateLimiter rateLimiter,
      TransactionTemplate transactionTemplate) {
    this.dao = dao;
    this.provider = provider;
    this.base = base;
    this.properties = properties;
    this.rateLimiter = rateLimiter;
    this.transactionTemplate = transactionTemplate;
  }

  public StarTourRouteResponse getPublicRoute(
      String tourSlug, List<String> requestedOptionalSlugs, String clientIp) {
    UUID tourId = dao.findPublishedTourId(tourSlug);
    return calculateIfNeeded(
        tourId, tourSlug, definition(tourId, requestedOptionalSlugs), false, clientIp);
  }

  /** Returns cached default routes without creating billable route-provider requests. */
  public List<StarTourRouteResponse> listPublicCachedRoutes() {
    return dao.findPublishedTours().stream()
        .map(this::cachedDefaultRouteFor)
        .flatMap(java.util.Optional::stream)
        .toList();
  }

  public RouteStatus recalculateForAdmin(UUID tourId) {
    dao.ensureTourExists(tourId);
    RouteDefinition definition = definition(tourId, List.of());
    if (definition.stops().isEmpty()) return RouteStatus.MISSING;
    return calculateIfNeeded(tourId, null, definition, true, null).routeStatus();
  }

  public RouteStatus statusFor(UUID tourId) {
    RouteDefinition definition = definition(tourId, List.of());
    if (definition.stops().isEmpty()) return RouteStatus.MISSING;
    RouteVariant variant = dao.findVariant(tourId, definition.fingerprint());
    if (variant == null) return dao.hasAnyVariant(tourId) ? RouteStatus.STALE : RouteStatus.MISSING;
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
    RouteVariant variant = dao.findVariant(tourId, definition.fingerprint());
    return variant != null && "FAILED".equals(variant.status()) ? variant.failureReason() : null;
  }

  private StarTourRouteResponse calculateIfNeeded(
      UUID tourId, String tourSlug, RouteDefinition definition, boolean force, String clientIp) {
    if (definition.stops().isEmpty())
      return response(tourSlug, definition, RouteStatus.MISSING, List.of(), true, null, null);
    RouteVariant existing = dao.findVariant(tourId, definition.fingerprint());
    List<RouteLeg> cached =
        existing == null ? null : storedLegs(existing.id(), definition.stops().size() + 1);
    if (!force && existing != null && "SUCCESS".equals(existing.status()) && cached != null)
      return response(tourSlug, definition, RouteStatus.READY, cached, true, null, null);
    if (!force
        && existing != null
        && "PENDING".equals(existing.status())
        && !pendingExpired(existing))
      return response(
          tourSlug,
          definition,
          RouteStatus.CALCULATING,
          List.of(),
          true,
          null,
          existing.calculatedAt());
    if (!force && existing != null && "FAILED".equals(existing.status()) && retryActive(existing))
      return response(
          tourSlug,
          definition,
          RouteStatus.FAILED,
          List.of(),
          true,
          existing.failureReason(),
          existing.retryAfter());
    if (!force) rateLimiter.consumeCalculation(clientIp);

    Claim claim = claimCalculation(tourId, definition, force);
    if (claim == Claim.CALCULATING) {
      RouteVariant pending = dao.findVariant(tourId, definition.fingerprint());
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
      RouteVariant ready = dao.findVariant(tourId, definition.fingerprint());
      List<RouteLeg> legs =
          ready == null ? null : storedLegs(ready.id(), definition.stops().size() + 1);
      if (legs != null)
        return response(tourSlug, definition, RouteStatus.READY, legs, true, null, null);
      claim = claimCalculation(tourId, definition, true);
      if (claim != Claim.OWNER)
        return response(tourSlug, definition, RouteStatus.CALCULATING, List.of(), true, null, null);
    }
    if (claim == Claim.FAILED) {
      RouteVariant failed = dao.findVariant(tourId, definition.fingerprint());
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
              definition.stops().stream().map(RouteTourStop::toRoutePoint).toList(),
              new RoutePoint(base.latitude(), base.longitude()));
      validateLegs(legs, definition.stops().size() + 1);
      dao.storeSuccess(tourId, definition, legs);
      return response(tourSlug, definition, RouteStatus.READY, legs, false, null, null);
    } catch (RuntimeException exception) {
      OffsetDateTime retryAfter = OffsetDateTime.now().plus(properties.failureCooldown());
      String failureReason = reason(exception);
      dao.storeFailure(tourId, definition, failureReason, retryAfter);
      return response(
          tourSlug, definition, RouteStatus.FAILED, List.of(), false, failureReason, retryAfter);
    }
  }

  private Claim claimCalculation(UUID tourId, RouteDefinition definition, boolean force) {
    return transactionTemplate.execute(
        ignored -> {
          RouteVariant variant = dao.findVariant(tourId, definition.fingerprint());
          if (variant == null) {
            if (dao.insertPending(tourId, definition) == 1) return Claim.OWNER;
            variant = dao.findVariant(tourId, definition.fingerprint());
            if (variant == null) throw new IllegalStateException("STAR_TOUR_ROUTE_CLAIM_FAILED");
          }
          if ("PENDING".equals(variant.status()) && !pendingExpired(variant))
            return Claim.CALCULATING;
          if (!force && "SUCCESS".equals(variant.status())) return Claim.READY;
          if (!force && "FAILED".equals(variant.status()) && retryActive(variant))
            return Claim.FAILED;
          return dao.markPending(variant.id(), properties.pendingTimeout()) == 1
              ? Claim.OWNER
              : Claim.CALCULATING;
        });
  }

  private java.util.Optional<StarTourRouteResponse> cachedDefaultRouteFor(PublishedStarTour tour) {
    RouteDefinition definition = definition(tour.id(), List.of());
    if (definition.stops().isEmpty()) return java.util.Optional.empty();
    RouteVariant variant = dao.findVariant(tour.id(), definition.fingerprint());
    if (variant == null || !"SUCCESS".equals(variant.status())) return java.util.Optional.empty();
    List<RouteLeg> legs = storedLegs(variant.id(), definition.stops().size() + 1);
    return legs == null
        ? java.util.Optional.empty()
        : java.util.Optional.of(
            response(tour.slug(), definition, RouteStatus.READY, legs, true, null, null));
  }

  private RouteDefinition definition(UUID tourId, List<String> requestedOptionalSlugs) {
    List<RouteTourStop> selected = selectStops(dao.findStops(tourId), requestedOptionalSlugs);
    if (selected.size() > MAX_INTERMEDIATE_STOPS)
      throw StarTourException.badRequest("STAR_TOUR_STOP_LIMIT_EXCEEDED");
    return new RouteDefinition(selected, selectionKey(selected), fingerprint(selected));
  }

  private static List<RouteTourStop> selectStops(
      List<RouteTourStop> available, List<String> requestedOptionalSlugs) {
    List<String> requested = requestedOptionalSlugs == null ? List.of() : requestedOptionalSlugs;
    if (requested.stream().anyMatch(slug -> slug == null || slug.isBlank()))
      throw StarTourException.badRequest("INVALID_OPTIONAL_STOPS");
    Set<String> selection = new HashSet<>(requested);
    if (selection.size() != requested.size())
      throw StarTourException.badRequest("INVALID_OPTIONAL_STOPS");
    Set<String> optional =
        available.stream()
            .filter(RouteTourStop::optional)
            .map(RouteTourStop::slug)
            .collect(java.util.stream.Collectors.toSet());
    if (!optional.containsAll(selection))
      throw StarTourException.badRequest("INVALID_OPTIONAL_STOPS");
    return available.stream()
        .filter(stop -> !stop.optional() || selection.contains(stop.slug()))
        .toList();
  }

  private String fingerprint(List<RouteTourStop> stops) {
    StringBuilder input =
        new StringBuilder("v2|base|")
            .append(decimal(base.latitude()))
            .append('|')
            .append(decimal(base.longitude()));
    for (RouteTourStop stop : stops)
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
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(input.toString().getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(64);
      for (byte value : digest) hex.append(String.format("%02x", value));
      return hex.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA_256_NOT_AVAILABLE", exception);
    }
  }

  private List<RouteLeg> storedLegs(UUID variantId, int expectedCount) {
    List<RouteLeg> legs = dao.findLegs(variantId);
    try {
      validateLegs(legs, expectedCount);
      return legs;
    } catch (RuntimeException exception) {
      return null;
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
    List<RouteLegResponse> legResponses =
        java.util.stream.IntStream.range(0, legs.size())
            .mapToObj(
                index -> {
                  RouteLeg leg = legs.get(index);
                  return new RouteLegResponse(
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
        new RouteBaseStop(base.latitude(), base.longitude()),
        java.util.stream.IntStream.range(0, definition.stops().size())
            .mapToObj(
                index -> {
                  RouteTourStop stop = definition.stops().get(index);
                  return new RouteStopResponse(
                      index + 1, stop.slug(), stop.latitude(), stop.longitude(), stop.optional());
                })
            .toList(),
        legResponses,
        legs.stream().mapToInt(RouteLeg::distanceMeters).sum(),
        legs.stream().mapToInt(RouteLeg::durationSeconds).sum(),
        failureReason,
        retryAfter);
  }

  private boolean pendingExpired(RouteVariant variant) {
    return !variant.calculatedAt().plus(properties.pendingTimeout()).isAfter(OffsetDateTime.now());
  }

  private static boolean retryActive(RouteVariant variant) {
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
                        || leg.encodedPolyline().isBlank()))
      throw new IllegalStateException("GOOGLE_ROUTES_INVALID_ROUTE_LEGS");
  }

  private static String selectionKey(List<RouteTourStop> stops) {
    return stops.stream()
        .filter(RouteTourStop::optional)
        .map(stop -> stop.id().toString())
        .sorted()
        .reduce((first, second) -> first + "," + second)
        .orElse("");
  }

  private static String decimal(BigDecimal value) {
    return value.stripTrailingZeros().toPlainString();
  }

  private static String reason(RuntimeException exception) {
    String value = exception.getMessage();
    String normalized = value == null || value.isBlank() ? "GOOGLE_ROUTES_REQUEST_FAILED" : value;
    return normalized.substring(0, Math.min(normalized.length(), 500));
  }

  private enum Claim {
    OWNER,
    READY,
    CALCULATING,
    FAILED
  }
}
