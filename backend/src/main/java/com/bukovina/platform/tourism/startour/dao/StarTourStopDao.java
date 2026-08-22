package com.bukovina.platform.tourism.startour.dao;

import com.bukovina.platform.tourism.startour.exception.StarTourException;
import com.bukovina.platform.tourism.startour.model.CoreTourStop;
import com.bukovina.platform.tourism.startour.model.DrivingMatrixLeg;
import com.bukovina.platform.tourism.startour.model.ValidatedTourStop;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class StarTourStopDao {
  private final JdbcClient jdbc;

  public StarTourStopDao(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public void ensureTourExists(UUID tourId) {
    boolean exists =
        jdbc.sql("SELECT EXISTS(SELECT 1 FROM star_tour WHERE id = :tourId)")
            .param("tourId", tourId)
            .query(Boolean.class)
            .single();
    if (!exists) throw StarTourException.notFound();
  }

  public void replaceCoreStops(UUID tourId, List<ValidatedTourStop> stops) {
    jdbc.sql(
            "UPDATE star_tour_attraction SET display_order = display_order + "
                + "(SELECT COALESCE(MAX(display_order), 0) + 1 FROM star_tour_attraction "
                + "WHERE star_tour_id = :tourId) WHERE star_tour_id = :tourId AND optional_stop = TRUE")
        .param("tourId", tourId)
        .update();
    jdbc.sql(
            "DELETE FROM star_tour_attraction WHERE star_tour_id = :tourId AND optional_stop = FALSE")
        .param("tourId", tourId)
        .update();
    for (int index = 0; index < stops.size(); index++) {
      ValidatedTourStop stop = stops.get(index);
      jdbc.sql(
              "INSERT INTO star_tour_attraction (star_tour_id, attraction_id, display_order, "
                  + "optional_stop, planned_visit_duration_minutes) "
                  + "VALUES (:tourId, :attractionId, :displayOrder, FALSE, :plannedDuration)")
          .param("tourId", tourId)
          .param("attractionId", stop.attractionId())
          .param("displayOrder", index)
          .param("plannedDuration", stop.plannedVisitDurationMinutes())
          .update();
    }
    jdbc.sql(
            "WITH ordered_optional AS (SELECT attraction_id, "
                + "ROW_NUMBER() OVER (ORDER BY display_order, attraction_id) - 1 AS optional_index "
                + "FROM star_tour_attraction WHERE star_tour_id = :tourId AND optional_stop = TRUE) "
                + "UPDATE star_tour_attraction assignment SET display_order = :coreStopCount + "
                + "ordered_optional.optional_index FROM ordered_optional "
                + "WHERE assignment.star_tour_id = :tourId "
                + "AND assignment.attraction_id = ordered_optional.attraction_id")
        .param("tourId", tourId)
        .param("coreStopCount", stops.size())
        .update();
  }

  public void unpublish(UUID tourId) {
    jdbc.sql(
            "UPDATE star_tour SET published = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = :tourId")
        .param("tourId", tourId)
        .update();
  }

  public List<CoreTourStop> findCoreStops(UUID tourId) {
    return jdbc.sql(
            "SELECT attraction.id, attraction.slug, translation.name, "
                + "attraction.recommended_visit_duration_minutes, assignment.planned_visit_duration_minutes "
                + "FROM star_tour_attraction assignment JOIN attraction ON attraction.id = assignment.attraction_id "
                + "AND attraction.active = TRUE JOIN attraction_translation translation "
                + "ON translation.attraction_id = attraction.id AND translation.language_code = 'hu' "
                + "WHERE assignment.star_tour_id = :tourId AND assignment.optional_stop = FALSE "
                + "ORDER BY assignment.display_order")
        .param("tourId", tourId)
        .query(
            (rs, row) ->
                new CoreTourStop(
                    rs.getObject("id", UUID.class),
                    rs.getString("slug"),
                    rs.getString("name"),
                    rs.getInt("recommended_visit_duration_minutes"),
                    rs.getObject("planned_visit_duration_minutes", Integer.class)))
        .list();
  }

  public List<UUID> findAssignedAttractionIds(UUID tourId) {
    return jdbc.sql("SELECT attraction_id FROM star_tour_attraction WHERE star_tour_id = :tourId")
        .param("tourId", tourId)
        .query(UUID.class)
        .list();
  }

  public boolean publishedState(UUID tourId) {
    return jdbc.sql("SELECT published FROM star_tour WHERE id = :tourId")
        .param("tourId", tourId)
        .query(Boolean.class)
        .single();
  }

  public DrivingMatrixLeg findDrivingLeg(UUID one, UUID other) {
    UUID first = one.toString().compareTo(other.toString()) < 0 ? one : other;
    UUID second = first.equals(one) ? other : one;
    return jdbc.sql(
            "SELECT distance_meters, duration_seconds, calculation_status, failure_reason "
                + "FROM attraction_driving_distance WHERE attraction_a_id = :first AND attraction_b_id = :second")
        .param("first", first)
        .param("second", second)
        .query(
            (rs, row) ->
                new DrivingMatrixLeg(
                    rs.getObject("distance_meters", Integer.class),
                    rs.getObject("duration_seconds", Integer.class),
                    rs.getString("calculation_status"),
                    rs.getString("failure_reason")))
        .optional()
        .orElse(new DrivingMatrixLeg(null, null, "MISSING", null));
  }

  public int countActiveAttractions(Set<UUID> ids) {
    return jdbc.sql("SELECT COUNT(*) FROM attraction WHERE active = TRUE AND id IN (:ids)")
        .param("ids", ids)
        .query(Integer.class)
        .single();
  }

  public int countOptionalAssignments(Set<UUID> ids) {
    return jdbc.sql(
            "SELECT COUNT(*) FROM star_tour_attraction WHERE optional_stop = TRUE "
                + "AND attraction_id IN (:ids)")
        .param("ids", ids)
        .query(Integer.class)
        .single();
  }
}
