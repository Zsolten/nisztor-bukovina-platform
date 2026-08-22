package com.bukovina.platform.tourism.startour.dao;

import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RouteLeg;
import com.bukovina.platform.tourism.startour.exception.StarTourException;
import com.bukovina.platform.tourism.startour.model.PublishedStarTour;
import com.bukovina.platform.tourism.startour.model.RouteDefinition;
import com.bukovina.platform.tourism.startour.model.RouteTourStop;
import com.bukovina.platform.tourism.startour.model.RouteVariant;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class StarTourRouteDao {
  private static final String SOURCE = "GOOGLE_ROUTES";
  private final JdbcClient jdbc;

  public StarTourRouteDao(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public List<PublishedStarTour> findPublishedTours() {
    return jdbc.sql(
            "SELECT id, slug FROM star_tour WHERE published = TRUE AND active = TRUE ORDER BY slug")
        .query(
            (rs, row) ->
                new PublishedStarTour(rs.getObject("id", UUID.class), rs.getString("slug")))
        .list();
  }

  public UUID findPublishedTourId(String slug) {
    return jdbc.sql(
            "SELECT id FROM star_tour WHERE slug = :slug AND published = TRUE AND active = TRUE")
        .param("slug", slug)
        .query(UUID.class)
        .optional()
        .orElseThrow(StarTourException::notFound);
  }

  public void ensureTourExists(UUID tourId) {
    boolean exists =
        jdbc.sql("SELECT EXISTS(SELECT 1 FROM star_tour WHERE id = :tourId)")
            .param("tourId", tourId)
            .query(Boolean.class)
            .single();
    if (!exists) throw StarTourException.notFound();
  }

  public List<RouteTourStop> findStops(UUID tourId) {
    return jdbc.sql(
            "SELECT attraction.id, attraction.slug, attraction.latitude, attraction.longitude, "
                + "assignment.display_order, assignment.optional_stop FROM star_tour_attraction assignment "
                + "JOIN attraction ON attraction.id = assignment.attraction_id "
                + "WHERE assignment.star_tour_id = :tourId AND attraction.active = TRUE "
                + "ORDER BY assignment.display_order")
        .param("tourId", tourId)
        .query(
            (rs, row) ->
                new RouteTourStop(
                    rs.getObject("id", UUID.class),
                    rs.getString("slug"),
                    rs.getBigDecimal("latitude"),
                    rs.getBigDecimal("longitude"),
                    rs.getInt("display_order"),
                    rs.getBoolean("optional_stop")))
        .list();
  }

  public RouteVariant findVariant(UUID tourId, String fingerprint) {
    return jdbc.sql(
            "SELECT id, calculation_status, calculated_at, failure_reason, retry_after "
                + "FROM star_tour_route_variant WHERE star_tour_id = :tourId AND route_fingerprint = :fingerprint")
        .param("tourId", tourId)
        .param("fingerprint", fingerprint)
        .query(
            (rs, row) ->
                new RouteVariant(
                    rs.getObject("id", UUID.class),
                    rs.getString("calculation_status"),
                    rs.getObject("calculated_at", OffsetDateTime.class),
                    rs.getString("failure_reason"),
                    rs.getObject("retry_after", OffsetDateTime.class)))
        .optional()
        .orElse(null);
  }

  public boolean hasAnyVariant(UUID tourId) {
    return jdbc.sql(
            "SELECT EXISTS(SELECT 1 FROM star_tour_route_variant WHERE star_tour_id = :tourId)")
        .param("tourId", tourId)
        .query(Boolean.class)
        .single();
  }

  public List<RouteLeg> findLegs(UUID variantId) {
    return jdbc.sql(
            "SELECT distance_meters, duration_seconds, encoded_polyline "
                + "FROM star_tour_route_leg WHERE route_variant_id = :variantId ORDER BY leg_order")
        .param("variantId", variantId)
        .query(
            (rs, row) ->
                new RouteLeg(
                    rs.getInt("distance_meters"),
                    rs.getInt("duration_seconds"),
                    rs.getString("encoded_polyline")))
        .list();
  }

  public int insertPending(UUID tourId, RouteDefinition definition) {
    return jdbc.sql(
            "INSERT INTO star_tour_route_variant (id, star_tour_id, selection_key, "
                + "route_fingerprint, calculation_status, source, calculated_at) "
                + "VALUES (:id, :tourId, :selectionKey, :fingerprint, 'PENDING', :source, :calculatedAt) "
                + "ON CONFLICT (star_tour_id, route_fingerprint) DO NOTHING")
        .param("id", UUID.randomUUID())
        .param("tourId", tourId)
        .param("selectionKey", definition.selectionKey())
        .param("fingerprint", definition.fingerprint())
        .param("source", SOURCE)
        .param("calculatedAt", OffsetDateTime.now())
        .update();
  }

  public int markPending(UUID variantId, Duration pendingTimeout) {
    return jdbc.sql(
            "UPDATE star_tour_route_variant SET calculation_status = 'PENDING', source = :source, "
                + "calculated_at = :calculatedAt, failure_reason = NULL, retry_after = NULL WHERE id = :id "
                + "AND (calculation_status <> 'PENDING' OR calculated_at < :pendingBefore)")
        .param("source", SOURCE)
        .param("calculatedAt", OffsetDateTime.now())
        .param("pendingBefore", OffsetDateTime.now().minus(pendingTimeout))
        .param("id", variantId)
        .update();
  }

  @Transactional
  public void storeSuccess(UUID tourId, RouteDefinition definition, List<RouteLeg> legs) {
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
  }

  @Transactional
  public void storeFailure(
      UUID tourId, RouteDefinition definition, String reason, OffsetDateTime retryAfter) {
    UUID variantId = variantId(tourId, definition.fingerprint());
    jdbc.sql(
            "UPDATE star_tour_route_variant SET calculation_status = 'FAILED', source = :source, "
                + "calculated_at = :calculatedAt, failure_reason = :failureReason, retry_after = :retryAfter WHERE id = :id")
        .param("source", SOURCE)
        .param("calculatedAt", OffsetDateTime.now())
        .param("failureReason", reason)
        .param("retryAfter", retryAfter)
        .param("id", variantId)
        .update();
    jdbc.sql("DELETE FROM star_tour_route_leg WHERE route_variant_id = :variantId")
        .param("variantId", variantId)
        .update();
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
}
