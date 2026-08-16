package com.bukovina.platform.tourism.startour.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Manages a star tour's ordered core stops and derives its planned itinerary totals. */
@Service
public class StarTourStopService {

  private static final int MIN_VISIT_DURATION_MINUTES = 5;
  private static final int MAX_VISIT_DURATION_MINUTES = 720;
  private static final int MAX_CORE_STOPS = 10;
  private final JdbcClient jdbc;

  public StarTourStopService(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Transactional(readOnly = true)
  public StopPlanResponse getPlan(UUID tourId) {
    ensureTourExists(tourId);
    return planFor(tourId);
  }

  @Transactional
  public StopPlanResponse replaceCoreStops(UUID tourId, StopPlanUpdate request) {
    ensureTourExists(tourId);
    List<ValidStop> stops = validate(request);

    jdbc.sql(
            "UPDATE star_tour_attraction SET display_order = display_order + 1000 "
                + "WHERE star_tour_id = :tourId AND optional_stop = TRUE")
        .param("tourId", tourId)
        .update();
    jdbc.sql(
            "DELETE FROM star_tour_attraction WHERE star_tour_id = :tourId AND optional_stop = FALSE")
        .param("tourId", tourId)
        .update();
    for (int index = 0; index < stops.size(); index++) {
      ValidStop stop = stops.get(index);
      jdbc.sql(
              "INSERT INTO star_tour_attraction (star_tour_id, attraction_id, display_order, optional_stop, "
                  + "planned_visit_duration_minutes) VALUES (:tourId, :attractionId, :displayOrder, FALSE, :plannedDuration)")
          .param("tourId", tourId)
          .param("attractionId", stop.attractionId())
          .param("displayOrder", index)
          .param("plannedDuration", stop.plannedVisitDurationMinutes())
          .update();
    }
    jdbc.sql(
            "UPDATE star_tour_attraction SET display_order = display_order - 1000 + :coreStopCount "
                + "WHERE star_tour_id = :tourId AND optional_stop = TRUE")
        .param("tourId", tourId)
        .param("coreStopCount", stops.size())
        .update();

    TourTotals totals = totalsFor(tourId);
    if (!totals.routeDataComplete()) {
      jdbc.sql(
              "UPDATE star_tour SET published = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = :tourId")
          .param("tourId", tourId)
          .update();
    }
    return planFor(tourId);
  }

  @Transactional(readOnly = true)
  public TourTotals totalsFor(UUID tourId) {
    List<CoreStopRow> stops = coreStopsFor(tourId);
    int visitDurationMinutes =
        stops.stream().mapToInt(CoreStopRow::effectiveVisitDurationMinutes).sum();
    if (stops.isEmpty()) {
      return new TourTotals(0, 0, visitDurationMinutes, visitDurationMinutes, false, List.of());
    }

    List<RouteLeg> routeLegs =
        java.util.stream.IntStream.range(0, stops.size() - 1)
            .mapToObj(index -> routeLeg(stops.get(index), stops.get(index + 1)))
            .toList();
    boolean routeDataComplete = routeLegs.stream().allMatch(leg -> "SUCCESS".equals(leg.status()));
    if (!routeDataComplete) {
      return new TourTotals(null, null, visitDurationMinutes, null, false, routeLegs);
    }
    int travelDistanceMeters = routeLegs.stream().mapToInt(RouteLeg::distanceMeters).sum();
    int travelDurationSeconds = routeLegs.stream().mapToInt(RouteLeg::durationSeconds).sum();
    return new TourTotals(
        travelDistanceMeters,
        travelDurationSeconds,
        visitDurationMinutes,
        travelDurationSeconds + visitDurationMinutes * 60,
        true,
        routeLegs);
  }

  @Transactional(readOnly = true)
  public void requirePublishable(UUID tourId) {
    TourTotals totals = totalsFor(tourId);
    if (!totals.routeDataComplete()) {
      throw badRequest("STAR_TOUR_ROUTE_DATA_INCOMPLETE");
    }
  }

  private StopPlanResponse planFor(UUID tourId) {
    List<CoreStopRow> stops = coreStopsFor(tourId);
    List<UUID> assignedAttractionIds =
        jdbc.sql("SELECT attraction_id FROM star_tour_attraction WHERE star_tour_id = :tourId")
            .param("tourId", tourId)
            .query(UUID.class)
            .list();
    boolean published =
        jdbc.sql("SELECT published FROM star_tour WHERE id = :tourId")
            .param("tourId", tourId)
            .query(Boolean.class)
            .single();
    return new StopPlanResponse(
        tourId,
        published,
        stops.stream()
            .map(
                stop ->
                    new AdminStop(
                        stop.attractionId(),
                        stop.slug(),
                        stop.name(),
                        stop.recommendedVisitDurationMinutes(),
                        stop.plannedVisitDurationMinutes(),
                        stop.effectiveVisitDurationMinutes()))
            .toList(),
        assignedAttractionIds,
        totalsFor(tourId));
  }

  private List<CoreStopRow> coreStopsFor(UUID tourId) {
    return jdbc.sql(
            "SELECT attraction.id, attraction.slug, translation.name, attraction.recommended_visit_duration_minutes, "
                + "assignment.planned_visit_duration_minutes FROM star_tour_attraction assignment "
                + "JOIN attraction ON attraction.id = assignment.attraction_id AND attraction.active = TRUE "
                + "JOIN attraction_translation translation ON translation.attraction_id = attraction.id "
                + "AND translation.language_code = 'hu' WHERE assignment.star_tour_id = :tourId "
                + "AND assignment.optional_stop = FALSE ORDER BY assignment.display_order")
        .param("tourId", tourId)
        .query(
            (rs, row) ->
                new CoreStopRow(
                    rs.getObject("id", UUID.class),
                    rs.getString("slug"),
                    rs.getString("name"),
                    rs.getInt("recommended_visit_duration_minutes"),
                    rs.getObject("planned_visit_duration_minutes", Integer.class)))
        .list();
  }

  private RouteLeg routeLeg(CoreStopRow from, CoreStopRow to) {
    AttractionPair pair = AttractionPair.of(from.attractionId(), to.attractionId());
    MatrixLeg row =
        jdbc.sql(
                "SELECT distance_meters, duration_seconds, calculation_status, failure_reason "
                    + "FROM attraction_driving_distance WHERE attraction_a_id = :first "
                    + "AND attraction_b_id = :second")
            .param("first", pair.first())
            .param("second", pair.second())
            .query(
                (rs, index) ->
                    new MatrixLeg(
                        rs.getObject("distance_meters", Integer.class),
                        rs.getObject("duration_seconds", Integer.class),
                        rs.getString("calculation_status"),
                        rs.getString("failure_reason")))
            .optional()
            .orElse(new MatrixLeg(null, null, "MISSING", null));
    return new RouteLeg(
        from.slug(),
        to.slug(),
        row.distanceMeters(),
        row.durationSeconds(),
        row.status(),
        row.failureReason());
  }

  private List<ValidStop> validate(StopPlanUpdate request) {
    if (request == null || request.stops() == null) {
      throw badRequest("INVALID_STAR_TOUR_STOPS");
    }
    if (request.stops().size() > MAX_CORE_STOPS) {
      throw badRequest("STAR_TOUR_STOP_LIMIT_EXCEEDED");
    }
    Set<UUID> ids = new HashSet<>();
    List<ValidStop> stops =
        request.stops().stream()
            .map(
                stop -> {
                  if (stop == null
                      || stop.attractionId() == null
                      || !ids.add(stop.attractionId())
                      || (stop.plannedVisitDurationMinutes() != null
                          && (stop.plannedVisitDurationMinutes() < MIN_VISIT_DURATION_MINUTES
                              || stop.plannedVisitDurationMinutes()
                                  > MAX_VISIT_DURATION_MINUTES))) {
                    throw badRequest("INVALID_STAR_TOUR_STOPS");
                  }
                  return new ValidStop(stop.attractionId(), stop.plannedVisitDurationMinutes());
                })
            .toList();
    if (stops.isEmpty()) {
      return stops;
    }
    Integer activeAttractionCount =
        jdbc.sql("SELECT COUNT(*) FROM attraction WHERE active = TRUE AND id IN (:ids)")
            .param("ids", ids)
            .query(Integer.class)
            .single();
    if (activeAttractionCount != stops.size()) {
      throw badRequest("UNKNOWN_OR_INACTIVE_ATTRACTION");
    }
    Integer optionalStopCount =
        jdbc.sql(
                "SELECT COUNT(*) FROM star_tour_attraction WHERE optional_stop = TRUE "
                    + "AND attraction_id IN (:ids)")
            .param("ids", ids)
            .query(Integer.class)
            .single();
    if (optionalStopCount > 0) {
      throw badRequest("ATTRACTION_ALREADY_ASSIGNED_AS_OPTIONAL_STOP");
    }
    return stops;
  }

  private void ensureTourExists(UUID tourId) {
    boolean exists =
        jdbc.sql("SELECT EXISTS(SELECT 1 FROM star_tour WHERE id = :tourId)")
            .param("tourId", tourId)
            .query(Boolean.class)
            .single();
    if (!exists) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "STAR_TOUR_NOT_FOUND");
    }
  }

  private static ResponseStatusException badRequest(String reason) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
  }

  public record StopPlanUpdate(List<StopAssignment> stops) {}

  public record StopAssignment(UUID attractionId, Integer plannedVisitDurationMinutes) {}

  public record StopPlanResponse(
      UUID tourId,
      boolean published,
      List<AdminStop> stops,
      List<UUID> assignedAttractionIds,
      TourTotals totals) {}

  public record AdminStop(
      UUID attractionId,
      String slug,
      String name,
      int recommendedVisitDurationMinutes,
      Integer plannedVisitDurationMinutes,
      int effectiveVisitDurationMinutes) {}

  public record TourTotals(
      Integer travelDistanceMeters,
      Integer travelDurationSeconds,
      int visitDurationMinutes,
      Integer totalDurationSeconds,
      boolean routeDataComplete,
      List<RouteLeg> routeLegs) {}

  public record RouteLeg(
      String fromSlug,
      String toSlug,
      Integer distanceMeters,
      Integer durationSeconds,
      String status,
      String failureReason) {}

  private record CoreStopRow(
      UUID attractionId,
      String slug,
      String name,
      int recommendedVisitDurationMinutes,
      Integer plannedVisitDurationMinutes) {
    private int effectiveVisitDurationMinutes() {
      return plannedVisitDurationMinutes == null
          ? recommendedVisitDurationMinutes
          : plannedVisitDurationMinutes;
    }
  }

  private record MatrixLeg(
      Integer distanceMeters, Integer durationSeconds, String status, String failureReason) {}

  private record ValidStop(UUID attractionId, Integer plannedVisitDurationMinutes) {}

  private record AttractionPair(UUID first, UUID second) {
    private static AttractionPair of(UUID one, UUID other) {
      return one.toString().compareTo(other.toString()) < 0
          ? new AttractionPair(one, other)
          : new AttractionPair(other, one);
    }
  }
}
