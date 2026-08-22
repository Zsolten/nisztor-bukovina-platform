package com.bukovina.platform.tourism.startour.service;

import com.bukovina.platform.tourism.startour.dao.StarTourStopDao;
import com.bukovina.platform.tourism.startour.dto.AdminStop;
import com.bukovina.platform.tourism.startour.dto.StopPlanResponse;
import com.bukovina.platform.tourism.startour.dto.StopPlanUpdate;
import com.bukovina.platform.tourism.startour.dto.TourRouteLeg;
import com.bukovina.platform.tourism.startour.dto.TourTotals;
import com.bukovina.platform.tourism.startour.exception.StarTourException;
import com.bukovina.platform.tourism.startour.model.CoreTourStop;
import com.bukovina.platform.tourism.startour.model.DrivingMatrixLeg;
import com.bukovina.platform.tourism.startour.model.ValidatedTourStop;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages a star tour's ordered core stops and derives its planned itinerary totals. */
@Service
public class StarTourStopService {
  private static final int MIN_VISIT_DURATION_MINUTES = 5;
  private static final int MAX_VISIT_DURATION_MINUTES = 720;
  private static final int MAX_CORE_STOPS = 10;
  private final StarTourStopDao dao;

  public StarTourStopService(StarTourStopDao dao) {
    this.dao = dao;
  }

  @Transactional(readOnly = true)
  public StopPlanResponse getPlan(UUID tourId) {
    dao.ensureTourExists(tourId);
    return planFor(tourId);
  }

  @Transactional
  public StopPlanResponse replaceCoreStops(UUID tourId, StopPlanUpdate request) {
    dao.ensureTourExists(tourId);
    List<ValidatedTourStop> stops = validate(request);
    dao.replaceCoreStops(tourId, stops);
    TourTotals totals = totalsFor(tourId);
    if (!totals.routeDataComplete()) dao.unpublish(tourId);
    return planFor(tourId);
  }

  @Transactional(readOnly = true)
  public TourTotals totalsFor(UUID tourId) {
    List<CoreTourStop> stops = dao.findCoreStops(tourId);
    int visitMinutes = stops.stream().mapToInt(CoreTourStop::effectiveVisitDurationMinutes).sum();
    if (stops.isEmpty()) return new TourTotals(0, 0, visitMinutes, visitMinutes, false, List.of());
    List<TourRouteLeg> legs =
        java.util.stream.IntStream.range(0, stops.size() - 1)
            .mapToObj(index -> routeLeg(stops.get(index), stops.get(index + 1)))
            .toList();
    boolean complete = legs.stream().allMatch(leg -> "SUCCESS".equals(leg.status()));
    if (!complete) return new TourTotals(null, null, visitMinutes, null, false, legs);
    int distance = legs.stream().mapToInt(TourRouteLeg::distanceMeters).sum();
    int duration = legs.stream().mapToInt(TourRouteLeg::durationSeconds).sum();
    return new TourTotals(
        distance, duration, visitMinutes, duration + visitMinutes * 60, true, legs);
  }

  @Transactional(readOnly = true)
  public void requirePublishable(UUID tourId) {
    if (!totalsFor(tourId).routeDataComplete())
      throw StarTourException.badRequest("STAR_TOUR_ROUTE_DATA_INCOMPLETE");
  }

  private StopPlanResponse planFor(UUID tourId) {
    List<CoreTourStop> stops = dao.findCoreStops(tourId);
    return new StopPlanResponse(
        tourId,
        dao.publishedState(tourId),
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
        dao.findAssignedAttractionIds(tourId),
        totalsFor(tourId));
  }

  private TourRouteLeg routeLeg(CoreTourStop from, CoreTourStop to) {
    DrivingMatrixLeg row = dao.findDrivingLeg(from.attractionId(), to.attractionId());
    return new TourRouteLeg(
        from.slug(),
        to.slug(),
        row.distanceMeters(),
        row.durationSeconds(),
        row.status(),
        row.failureReason());
  }

  private List<ValidatedTourStop> validate(StopPlanUpdate request) {
    if (request == null || request.stops() == null)
      throw StarTourException.badRequest("INVALID_STAR_TOUR_STOPS");
    if (request.stops().size() > MAX_CORE_STOPS)
      throw StarTourException.badRequest("STAR_TOUR_STOP_LIMIT_EXCEEDED");
    Set<UUID> ids = new HashSet<>();
    List<ValidatedTourStop> stops =
        request.stops().stream()
            .map(
                stop -> {
                  if (stop == null
                      || stop.attractionId() == null
                      || !ids.add(stop.attractionId())
                      || (stop.plannedVisitDurationMinutes() != null
                          && (stop.plannedVisitDurationMinutes() < MIN_VISIT_DURATION_MINUTES
                              || stop.plannedVisitDurationMinutes() > MAX_VISIT_DURATION_MINUTES)))
                    throw StarTourException.badRequest("INVALID_STAR_TOUR_STOPS");
                  return new ValidatedTourStop(
                      stop.attractionId(), stop.plannedVisitDurationMinutes());
                })
            .toList();
    if (!stops.isEmpty() && dao.countActiveAttractions(ids) != stops.size())
      throw StarTourException.badRequest("UNKNOWN_OR_INACTIVE_ATTRACTION");
    if (!stops.isEmpty() && dao.countOptionalAssignments(ids) > 0)
      throw StarTourException.badRequest("ATTRACTION_ALREADY_ASSIGNED_AS_OPTIONAL_STOP");
    return stops;
  }
}
