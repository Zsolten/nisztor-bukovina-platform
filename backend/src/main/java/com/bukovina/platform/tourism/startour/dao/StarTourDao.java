package com.bukovina.platform.tourism.startour.dao;

import com.bukovina.platform.tourism.startour.exception.StarTourException;
import com.bukovina.platform.tourism.startour.model.PublicStarTour;
import com.bukovina.platform.tourism.startour.model.PublicTourStop;
import com.bukovina.platform.tourism.startour.model.StarTour;
import com.bukovina.platform.tourism.startour.model.StarTourContent;
import com.bukovina.platform.tourism.startour.model.StarTourImageData;
import com.bukovina.platform.tourism.startour.model.ValidatedStarTour;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class StarTourDao {
  private final JdbcClient jdbc;

  public StarTourDao(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public List<UUID> findAllIds() {
    return jdbc.sql("SELECT id FROM star_tour ORDER BY updated_at DESC, slug")
        .query(UUID.class)
        .list();
  }

  public StarTour findById(UUID id) {
    return jdbc.sql("SELECT id, slug, map_color, published, active FROM star_tour WHERE id = :id")
        .param("id", id)
        .query(
            (rs, row) ->
                new StarTour(
                    rs.getObject("id", UUID.class),
                    rs.getString("slug"),
                    rs.getString("map_color"),
                    rs.getBoolean("published"),
                    rs.getBoolean("active")))
        .optional()
        .orElseThrow(StarTourException::notFound);
  }

  public void insert(UUID id, ValidatedStarTour tour) {
    jdbc.sql(
            "INSERT INTO star_tour (id, slug, map_color, published, active) "
                + "VALUES (:id, :slug, :color, :published, :active)")
        .param("id", id)
        .param("slug", tour.slug())
        .param("color", tour.mapColor())
        .param("published", tour.published())
        .param("active", tour.active())
        .update();
  }

  public void update(UUID id, ValidatedStarTour tour) {
    jdbc.sql(
            "UPDATE star_tour SET slug = :slug, map_color = :color, published = :published, "
                + "active = :active, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
        .param("id", id)
        .param("slug", tour.slug())
        .param("color", tour.mapColor())
        .param("published", tour.published())
        .param("active", tour.active())
        .update();
  }

  public boolean slugExists(String slug, UUID excludedId) {
    if (excludedId == null)
      return jdbc.sql("SELECT EXISTS(SELECT 1 FROM star_tour WHERE slug = :slug)")
          .param("slug", slug)
          .query(Boolean.class)
          .single();
    return jdbc.sql("SELECT EXISTS(SELECT 1 FROM star_tour WHERE slug = :slug AND id <> :id)")
        .param("slug", slug)
        .param("id", excludedId)
        .query(Boolean.class)
        .single();
  }

  public boolean publishedState(UUID id) {
    return findById(id).published();
  }

  public List<PublicStarTour> findAllPublic(String language) {
    return jdbc.sql(
            publicSelect() + " WHERE tour.published = TRUE AND tour.active = TRUE ORDER BY name")
        .param("language", language)
        .query(this::mapPublic)
        .list();
  }

  public PublicStarTour findPublicBySlug(String slug, String language) {
    return jdbc.sql(
            publicSelect()
                + " WHERE tour.published = TRUE AND tour.active = TRUE AND tour.slug = :slug")
        .param("language", language)
        .param("slug", slug)
        .query(this::mapPublic)
        .optional()
        .orElseThrow(StarTourException::notFound);
  }

  public List<StarTourContent> findTranslations(UUID id) {
    return jdbc.sql(
            "SELECT language_code, name, short_description, detailed_description "
                + "FROM star_tour_translation WHERE star_tour_id = :id ORDER BY language_code")
        .param("id", id)
        .query(
            (rs, row) ->
                new StarTourContent(
                    rs.getString("language_code"),
                    rs.getString("name"),
                    rs.getString("short_description"),
                    rs.getString("detailed_description")))
        .list();
  }

  public List<String> findTags(UUID id) {
    return jdbc.sql("SELECT tag FROM star_tour_tag WHERE star_tour_id = :id ORDER BY tag")
        .param("id", id)
        .query(String.class)
        .list();
  }

  public List<StarTourImageData> findImages(UUID id, String language) {
    return jdbc.sql(
            "SELECT image.image_url, requested.alt_text FROM star_tour_image image "
                + "JOIN star_tour_image_translation requested ON requested.image_id = image.id "
                + "AND requested.language_code = :language WHERE image.star_tour_id = :id "
                + "ORDER BY image.display_order")
        .param("language", language)
        .param("id", id)
        .query(
            (rs, row) -> new StarTourImageData(rs.getString("image_url"), rs.getString("alt_text")))
        .list();
  }

  public List<StarTourImageData> findAdminImages(UUID id) {
    return jdbc.sql(
            "SELECT image.image_url, COALESCE(hu.alt_text, '') alt_text "
                + "FROM star_tour_image image LEFT JOIN star_tour_image_translation hu "
                + "ON hu.image_id = image.id AND hu.language_code = 'hu' "
                + "WHERE image.star_tour_id = :id ORDER BY image.display_order")
        .param("id", id)
        .query(
            (rs, row) -> new StarTourImageData(rs.getString("image_url"), rs.getString("alt_text")))
        .list();
  }

  public List<PublicTourStop> findStops(UUID id, String language) {
    return jdbc.sql(
            "SELECT attraction.slug, translation.name, attraction.latitude, attraction.longitude, "
                + "attraction.google_maps_url, assignment.optional_stop, "
                + "COALESCE(assignment.planned_visit_duration_minutes, attraction.recommended_visit_duration_minutes) "
                + "AS visit_duration_minutes FROM star_tour_attraction assignment "
                + "JOIN attraction ON attraction.id = assignment.attraction_id AND attraction.active = TRUE "
                + "JOIN attraction_translation translation ON translation.attraction_id = attraction.id "
                + "AND translation.language_code = :language WHERE assignment.star_tour_id = :id "
                + "ORDER BY assignment.display_order")
        .param("language", language)
        .param("id", id)
        .query(
            (rs, row) ->
                new PublicTourStop(
                    rs.getString("slug"),
                    rs.getString("name"),
                    rs.getBigDecimal("latitude"),
                    rs.getBigDecimal("longitude"),
                    rs.getString("google_maps_url"),
                    rs.getBoolean("optional_stop"),
                    rs.getInt("visit_duration_minutes")))
        .list();
  }

  public void replaceChildren(UUID id, ValidatedStarTour tour) {
    jdbc.sql("DELETE FROM star_tour_translation WHERE star_tour_id = :id").param("id", id).update();
    for (StarTourContent translation : tour.translations().values()) {
      if (!"hu".equals(translation.language()) && blank(translation.name())) continue;
      jdbc.sql(
              "INSERT INTO star_tour_translation (star_tour_id, language_code, name, short_description, detailed_description) "
                  + "VALUES (:id, :language, :name, :short, :detailed)")
          .param("id", id)
          .param("language", translation.language())
          .param("name", translation.name().trim())
          .param("short", trim(translation.shortDescription()))
          .param("detailed", trim(translation.detailedDescription()))
          .update();
    }
    jdbc.sql("DELETE FROM star_tour_tag WHERE star_tour_id = :id").param("id", id).update();
    for (String tag : tour.tags())
      jdbc.sql("INSERT INTO star_tour_tag (star_tour_id, tag) VALUES (:id, :tag)")
          .param("id", id)
          .param("tag", tag)
          .update();
    jdbc.sql("DELETE FROM star_tour_image WHERE star_tour_id = :id").param("id", id).update();
    for (int index = 0; index < tour.images().size(); index++) {
      StarTourImageData image = tour.images().get(index);
      UUID imageId = UUID.randomUUID();
      jdbc.sql(
              "INSERT INTO star_tour_image (id, star_tour_id, image_url, display_order) "
                  + "VALUES (:imageId, :id, :url, :position)")
          .param("imageId", imageId)
          .param("id", id)
          .param("url", image.imageUrl().trim())
          .param("position", index)
          .update();
      if (!blank(image.altText()))
        jdbc.sql(
                "INSERT INTO star_tour_image_translation (image_id, language_code, alt_text) "
                    + "VALUES (:imageId, 'hu', :altText)")
            .param("imageId", imageId)
            .param("altText", image.altText().trim())
            .update();
    }
  }

  private String publicSelect() {
    return "SELECT tour.id, tour.slug, tour.map_color, requested.name, requested.short_description, "
        + "requested.detailed_description FROM star_tour tour JOIN star_tour_translation requested "
        + "ON requested.star_tour_id = tour.id AND requested.language_code = :language";
  }

  private PublicStarTour mapPublic(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
    return new PublicStarTour(
        rs.getObject("id", UUID.class),
        rs.getString("slug"),
        rs.getString("name"),
        rs.getString("short_description"),
        rs.getString("detailed_description"),
        rs.getString("map_color"));
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  private static String trim(String value) {
    return value == null ? "" : value.trim();
  }
}
