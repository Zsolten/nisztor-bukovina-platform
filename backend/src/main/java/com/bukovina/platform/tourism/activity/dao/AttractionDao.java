package com.bukovina.platform.tourism.activity.dao;

import com.bukovina.platform.tourism.activity.exception.AttractionException;
import com.bukovina.platform.tourism.activity.model.Attraction;
import com.bukovina.platform.tourism.activity.model.AttractionContent;
import com.bukovina.platform.tourism.activity.model.PublicAttraction;
import com.bukovina.platform.tourism.activity.model.TourismCollection;
import com.bukovina.platform.tourism.activity.model.ValidatedAttraction;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AttractionDao {
  private final JdbcClient jdbc;

  public AttractionDao(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public List<UUID> findAllIds() {
    return jdbc.sql("SELECT id FROM attraction ORDER BY updated_at DESC, slug")
        .query(UUID.class)
        .list();
  }

  public void insert(UUID id, ValidatedAttraction attraction) {
    jdbc.sql(
            "INSERT INTO attraction (id, slug, latitude, longitude, google_maps_url, "
                + "recommended_visit_duration_minutes, active) "
                + "VALUES (:id, :slug, :latitude, :longitude, :mapsUrl, :duration, :active)")
        .param("id", id)
        .param("slug", attraction.slug())
        .param("latitude", attraction.latitude())
        .param("longitude", attraction.longitude())
        .param("mapsUrl", attraction.googleMapsUrl())
        .param("duration", attraction.recommendedVisitDurationMinutes())
        .param("active", attraction.active())
        .update();
  }

  public void update(UUID id, ValidatedAttraction attraction) {
    jdbc.sql(
            "UPDATE attraction SET slug = :slug, latitude = :latitude, longitude = :longitude, "
                + "google_maps_url = :mapsUrl, recommended_visit_duration_minutes = :duration, "
                + "active = :active, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
        .param("id", id)
        .param("slug", attraction.slug())
        .param("latitude", attraction.latitude())
        .param("longitude", attraction.longitude())
        .param("mapsUrl", attraction.googleMapsUrl())
        .param("duration", attraction.recommendedVisitDurationMinutes())
        .param("active", attraction.active())
        .update();
  }

  public Attraction findById(UUID id) {
    return jdbc.sql(
            "SELECT id, slug, latitude, longitude, google_maps_url, recommended_visit_duration_minutes, active "
                + "FROM attraction WHERE id = :id")
        .param("id", id)
        .query(
            (rs, row) ->
                new Attraction(
                    rs.getObject("id", UUID.class),
                    rs.getString("slug"),
                    rs.getBigDecimal("latitude"),
                    rs.getBigDecimal("longitude"),
                    rs.getString("google_maps_url"),
                    rs.getInt("recommended_visit_duration_minutes"),
                    rs.getBoolean("active")))
        .optional()
        .orElseThrow(AttractionException::notFound);
  }

  public boolean slugExists(String slug, UUID excludedId) {
    if (excludedId == null) {
      return jdbc.sql("SELECT EXISTS(SELECT 1 FROM attraction WHERE slug = :slug)")
          .param("slug", slug)
          .query(Boolean.class)
          .single();
    }
    return jdbc.sql("SELECT EXISTS(SELECT 1 FROM attraction WHERE slug = :slug AND id <> :id)")
        .param("slug", slug)
        .param("id", excludedId)
        .query(Boolean.class)
        .single();
  }

  public boolean collectionExists(String slug) {
    return jdbc.sql("SELECT EXISTS(SELECT 1 FROM tourism_collection WHERE slug = :slug)")
        .param("slug", slug)
        .query(Boolean.class)
        .single();
  }

  public List<AttractionContent> findTranslations(UUID id) {
    return jdbc.sql(
            "SELECT language_code, name, short_description, detailed_description, "
                + "admission_information, practical_information FROM attraction_translation "
                + "WHERE attraction_id = :id ORDER BY language_code")
        .param("id", id)
        .query(
            (rs, row) ->
                new AttractionContent(
                    rs.getString("language_code"), rs.getString("name"),
                    rs.getString("short_description"), rs.getString("detailed_description"),
                    rs.getString("admission_information"), rs.getString("practical_information")))
        .list();
  }

  public List<String> findCollectionSlugs(UUID id, boolean activeOnly) {
    String activeClause = activeOnly ? " AND collection.active = TRUE" : "";
    return jdbc.sql(
            "SELECT collection.slug FROM attraction_collection assignment "
                + "JOIN tourism_collection collection ON collection.id = assignment.collection_id "
                + "WHERE assignment.attraction_id = :id"
                + activeClause
                + " ORDER BY assignment.display_order")
        .param("id", id)
        .query(String.class)
        .list();
  }

  public List<PublicAttraction> findAllPublic(String language) {
    return jdbc.sql(publicSelect() + " WHERE attraction.active = TRUE ORDER BY name")
        .param("language", language)
        .query(this::mapPublic)
        .list();
  }

  public PublicAttraction findPublicBySlug(String slug, String language) {
    return jdbc.sql(publicSelect() + " WHERE attraction.active = TRUE AND attraction.slug = :slug")
        .param("language", language)
        .param("slug", slug)
        .query(this::mapPublic)
        .optional()
        .orElseThrow(AttractionException::notFound);
  }

  public List<TourismCollection> findActiveCollections(String language) {
    return jdbc.sql(
            "SELECT collection.slug, requested.name, requested.short_description "
                + "FROM tourism_collection collection "
                + "JOIN tourism_collection_translation requested ON requested.collection_id = collection.id "
                + "AND requested.language_code = :language WHERE collection.active = TRUE "
                + "ORDER BY collection.display_order")
        .param("language", language)
        .query(
            (rs, row) ->
                new TourismCollection(
                    rs.getString("slug"), rs.getString("name"), rs.getString("short_description")))
        .list();
  }

  public void replaceChildren(UUID id, ValidatedAttraction attraction) {
    jdbc.sql("DELETE FROM attraction_translation WHERE attraction_id = :id")
        .param("id", id)
        .update();
    for (AttractionContent translation : attraction.translations().values()) {
      if (!"hu".equals(translation.language()) && blank(translation.name())) continue;
      jdbc.sql(
              "INSERT INTO attraction_translation (attraction_id, language_code, name, short_description, "
                  + "detailed_description, admission_information, practical_information) "
                  + "VALUES (:id, :language, :name, :short, :detailed, :admission, :practical)")
          .param("id", id)
          .param("language", translation.language())
          .param("name", trim(translation.name()))
          .param("short", trim(translation.shortDescription()))
          .param("detailed", trim(translation.detailedDescription()))
          .param("admission", nullable(translation.admissionInformation()))
          .param("practical", nullable(translation.practicalInformation()))
          .update();
    }
    Map<String, Integer> orders = new LinkedHashMap<>();
    jdbc.sql(
            "SELECT collection.slug, assignment.display_order FROM attraction_collection assignment "
                + "JOIN tourism_collection collection ON collection.id = assignment.collection_id "
                + "WHERE assignment.attraction_id = :id")
        .param("id", id)
        .query((rs, row) -> Map.entry(rs.getString("slug"), rs.getInt("display_order")))
        .list()
        .forEach(entry -> orders.put(entry.getKey(), entry.getValue()));
    jdbc.sql("DELETE FROM attraction_collection WHERE attraction_id = :id")
        .param("id", id)
        .update();
    for (String slug : attraction.collectionSlugs()) {
      Integer existingOrder = orders.get(slug);
      int order = existingOrder == null ? nextOrder(slug) : existingOrder;
      jdbc.sql(
              "INSERT INTO attraction_collection (attraction_id, collection_id, display_order) "
                  + "SELECT :id, id, :displayOrder FROM tourism_collection WHERE slug = :slug")
          .param("id", id)
          .param("displayOrder", order)
          .param("slug", slug)
          .update();
    }
  }

  private int nextOrder(String slug) {
    return jdbc.sql(
            "SELECT COALESCE(MAX(assignment.display_order), 0) + 10 "
                + "FROM attraction_collection assignment "
                + "JOIN tourism_collection collection ON collection.id = assignment.collection_id "
                + "WHERE collection.slug = :slug")
        .param("slug", slug)
        .query(Integer.class)
        .single();
  }

  private String publicSelect() {
    return "SELECT attraction.id, attraction.slug, attraction.latitude, attraction.longitude, attraction.google_maps_url, "
        + "attraction.recommended_visit_duration_minutes, requested.name, requested.short_description, "
        + "requested.detailed_description, requested.admission_information, requested.practical_information "
        + "FROM attraction JOIN attraction_translation requested ON requested.attraction_id = attraction.id "
        + "AND requested.language_code = :language";
  }

  private PublicAttraction mapPublic(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
    return new PublicAttraction(
        rs.getObject("id", UUID.class),
        rs.getString("slug"),
        rs.getString("name"),
        rs.getString("short_description"),
        rs.getString("detailed_description"),
        rs.getString("admission_information"),
        rs.getString("practical_information"),
        rs.getBigDecimal("latitude"),
        rs.getBigDecimal("longitude"),
        rs.getString("google_maps_url"),
        rs.getInt("recommended_visit_duration_minutes"));
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  private static String trim(String value) {
    return value == null ? "" : value.trim();
  }

  private static String nullable(String value) {
    return blank(value) ? null : value.trim();
  }
}
