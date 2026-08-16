package com.bukovina.platform.tourism.activity.service;

import com.bukovina.platform.tourism.routing.DrivingDistanceMatrixService;
import com.bukovina.platform.tourism.routing.DrivingDistanceMatrixService.CalculationSummary;
import java.math.BigDecimal;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttractionService {

  private static final Set<String> LANGUAGES = Set.of("hu", "ro", "en");
  private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
  private final JdbcClient jdbc;
  private final DrivingDistanceMatrixService drivingDistanceMatrix;

  public AttractionService(JdbcClient jdbc, DrivingDistanceMatrixService drivingDistanceMatrix) {
    this.jdbc = jdbc;
    this.drivingDistanceMatrix = drivingDistanceMatrix;
  }

  @Transactional(readOnly = true)
  public List<AttractionResponse> listAdmin() {
    return jdbc
        .sql("SELECT id FROM attraction ORDER BY updated_at DESC, slug")
        .query(UUID.class)
        .list()
        .stream()
        .map(id -> findAdmin(id, null))
        .toList();
  }

  @Transactional
  public AttractionResponse create(AttractionUpsertRequest request) {
    ValidAttraction valid = validate(request, null);
    UUID id = UUID.randomUUID();
    jdbc.sql(
            "INSERT INTO attraction (id, slug, latitude, longitude, google_maps_url, active) "
                + "VALUES (:id, :slug, :latitude, :longitude, :mapsUrl, :active)")
        .param("id", id)
        .param("slug", valid.slug())
        .param("latitude", valid.latitude())
        .param("longitude", valid.longitude())
        .param("mapsUrl", valid.googleMapsUrl())
        .param("active", valid.active())
        .update();
    replaceChildren(id, valid);
    return findAdmin(id, drivingDistanceMatrix.recalculateAffectedPairs(id));
  }

  @Transactional
  public AttractionResponse update(UUID id, AttractionUpsertRequest request) {
    AttractionRow existing = findRow(id);
    ValidAttraction valid = validate(request, id);
    jdbc.sql(
            "UPDATE attraction SET slug = :slug, latitude = :latitude, longitude = :longitude, "
                + "google_maps_url = :mapsUrl, active = :active, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
        .param("id", id)
        .param("slug", valid.slug())
        .param("latitude", valid.latitude())
        .param("longitude", valid.longitude())
        .param("mapsUrl", valid.googleMapsUrl())
        .param("active", valid.active())
        .update();
    replaceChildren(id, valid);
    CalculationSummary calculation =
        coordinatesChanged(existing, valid)
            ? drivingDistanceMatrix.recalculateAffectedPairs(id)
            : null;
    return findAdmin(id, calculation);
  }

  @Transactional(readOnly = true)
  public List<AttractionPublicResponse> listPublic(String language) {
    return jdbc
        .sql(publicSelect() + " WHERE attraction.active = TRUE ORDER BY name")
        .param("language", language)
        .query(this::mapPublicRow)
        .list()
        .stream()
        .map(this::toPublicResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public AttractionPublicResponse getPublic(String slug, String language) {
    return jdbc.sql(publicSelect() + " WHERE attraction.active = TRUE AND attraction.slug = :slug")
        .param("language", language)
        .param("slug", slug)
        .query(this::mapPublicRow)
        .optional()
        .map(this::toPublicResponse)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ATTRACTION_NOT_FOUND"));
  }

  private AttractionResponse findAdmin(UUID id, CalculationSummary calculation) {
    AttractionRow row = findRow(id);
    List<Translation> translations =
        jdbc.sql(
                "SELECT language_code, name, short_description, detailed_description, "
                    + "admission_information, practical_information FROM attraction_translation "
                    + "WHERE attraction_id = :id ORDER BY language_code")
            .param("id", id)
            .query(
                (rs, n) ->
                    new Translation(
                        rs.getString("language_code"),
                        rs.getString("name"),
                        rs.getString("short_description"),
                        rs.getString("detailed_description"),
                        rs.getString("admission_information"),
                        rs.getString("practical_information")))
            .list();
    List<String> collections =
        jdbc.sql(
                "SELECT collection.slug FROM attraction_collection assignment "
                    + "JOIN tourism_collection collection ON collection.id = assignment.collection_id "
                    + "WHERE assignment.attraction_id = :id ORDER BY assignment.display_order")
            .param("id", id)
            .query(String.class)
            .list();
    return new AttractionResponse(
        row.id(),
        row.slug(),
        row.latitude(),
        row.longitude(),
        row.googleMapsUrl(),
        row.active(),
        translations,
        collections,
        calculation);
  }

  private AttractionRow findRow(UUID id) {
    return jdbc.sql(
            "SELECT id, slug, latitude, longitude, google_maps_url, active "
                + "FROM attraction WHERE id = :id")
        .param("id", id)
        .query(
            (rs, n) ->
                new AttractionRow(
                    rs.getObject("id", UUID.class),
                    rs.getString("slug"),
                    rs.getBigDecimal("latitude"),
                    rs.getBigDecimal("longitude"),
                    rs.getString("google_maps_url"),
                    rs.getBoolean("active")))
        .optional()
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ATTRACTION_NOT_FOUND"));
  }

  private ValidAttraction validate(AttractionUpsertRequest request, UUID currentId) {
    if (request == null
        || blank(request.slug())
        || !SLUG.matcher(request.slug().trim()).matches()) {
      throw badRequest("INVALID_ATTRACTION_SLUG");
    }
    String slug = request.slug().trim();
    Integer duplicate =
        currentId == null
            ? jdbc.sql("SELECT COUNT(*) FROM attraction WHERE slug = :slug")
                .param("slug", slug)
                .query(Integer.class)
                .single()
            : jdbc.sql("SELECT COUNT(*) FROM attraction WHERE slug = :slug AND id <> :id")
                .param("slug", slug)
                .param("id", currentId)
                .query(Integer.class)
                .single();
    if (duplicate > 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "ATTRACTION_SLUG_EXISTS");
    }
    if (request.latitude() == null
        || request.latitude().compareTo(BigDecimal.valueOf(-90)) < 0
        || request.latitude().compareTo(BigDecimal.valueOf(90)) > 0) {
      throw badRequest("INVALID_LATITUDE");
    }
    if (request.longitude() == null
        || request.longitude().compareTo(BigDecimal.valueOf(-180)) < 0
        || request.longitude().compareTo(BigDecimal.valueOf(180)) > 0) {
      throw badRequest("INVALID_LONGITUDE");
    }
    validateHttpUrl(request.googleMapsUrl(), "INVALID_GOOGLE_MAPS_URL");
    if (request.active() == null) {
      throw badRequest("ACTIVE_REQUIRED");
    }

    Map<String, Translation> translations = translations(request.translations());
    Translation hu = translations.get("hu");
    if (hu == null
        || blank(hu.name())
        || blank(hu.shortDescription())
        || blank(hu.detailedDescription())) {
      throw badRequest("HUNGARIAN_ATTRACTION_CONTENT_REQUIRED");
    }
    List<String> collectionSlugs =
        request.collectionSlugs() == null
            ? List.of()
            : request.collectionSlugs().stream().map(String::trim).distinct().toList();
    for (String collectionSlug : collectionSlugs) {
      boolean exists =
          jdbc.sql("SELECT EXISTS(SELECT 1 FROM tourism_collection WHERE slug = :slug)")
              .param("slug", collectionSlug)
              .query(Boolean.class)
              .single();
      if (!exists) {
        throw badRequest("UNKNOWN_TOURISM_COLLECTION");
      }
    }
    return new ValidAttraction(
        slug,
        request.latitude(),
        request.longitude(),
        request.googleMapsUrl().trim(),
        request.active(),
        translations,
        collectionSlugs);
  }

  private Map<String, Translation> translations(List<Translation> input) {
    if (input == null) {
      throw badRequest("HUNGARIAN_ATTRACTION_CONTENT_REQUIRED");
    }
    Map<String, Translation> result = new LinkedHashMap<>();
    for (Translation translation : input) {
      if (translation == null
          || !LANGUAGES.contains(translation.language())
          || result.put(translation.language(), translation) != null) {
        throw badRequest("INVALID_TRANSLATIONS");
      }
    }
    return result;
  }

  private void replaceChildren(UUID id, ValidAttraction valid) {
    jdbc.sql("DELETE FROM attraction_translation WHERE attraction_id = :id")
        .param("id", id)
        .update();
    for (Translation translation : valid.translations().values()) {
      if (!"hu".equals(translation.language()) && blank(translation.name())) {
        continue;
      }
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
    jdbc.sql("DELETE FROM attraction_collection WHERE attraction_id = :id")
        .param("id", id)
        .update();
    for (int index = 0; index < valid.collectionSlugs().size(); index++) {
      jdbc.sql(
              "INSERT INTO attraction_collection (attraction_id, collection_id, display_order) "
                  + "SELECT :id, id, :displayOrder FROM tourism_collection WHERE slug = :slug")
          .param("id", id)
          .param("displayOrder", index)
          .param("slug", valid.collectionSlugs().get(index))
          .update();
    }
  }

  private String publicSelect() {
    return "SELECT attraction.id, attraction.slug, attraction.latitude, attraction.longitude, attraction.google_maps_url, "
        + "requested.name, requested.short_description, requested.detailed_description, "
        + "requested.admission_information, requested.practical_information "
        + "FROM attraction JOIN attraction_translation requested ON requested.attraction_id = attraction.id "
        + "AND requested.language_code = :language";
  }

  private PublicAttractionRow mapPublicRow(java.sql.ResultSet rs, int row)
      throws java.sql.SQLException {
    return new PublicAttractionRow(
        rs.getObject("id", UUID.class),
        rs.getString("slug"),
        rs.getString("name"),
        rs.getString("short_description"),
        rs.getString("detailed_description"),
        rs.getString("admission_information"),
        rs.getString("practical_information"),
        rs.getBigDecimal("latitude"),
        rs.getBigDecimal("longitude"),
        rs.getString("google_maps_url"));
  }

  @Transactional(readOnly = true)
  public List<CollectionResponse> listCollections(String language) {
    return jdbc.sql(
            "SELECT collection.slug, requested.name, requested.short_description "
                + "FROM tourism_collection collection "
                + "JOIN tourism_collection_translation requested ON requested.collection_id = collection.id "
                + "AND requested.language_code = :language WHERE collection.active = TRUE ORDER BY collection.display_order")
        .param("language", language)
        .query(
            (rs, row) ->
                new CollectionResponse(
                    rs.getString("slug"), rs.getString("name"), rs.getString("short_description")))
        .list();
  }

  private AttractionPublicResponse toPublicResponse(PublicAttractionRow row) {
    return new AttractionPublicResponse(
        row.slug(),
        row.name(),
        row.shortDescription(),
        row.detailedDescription(),
        row.admissionInformation(),
        row.practicalInformation(),
        row.latitude(),
        row.longitude(),
        row.googleMapsUrl(),
        collectionSlugsFor(row.id()));
  }

  private List<String> collectionSlugsFor(UUID id) {
    return jdbc.sql(
            "SELECT collection.slug FROM attraction_collection assignment "
                + "JOIN tourism_collection collection ON collection.id = assignment.collection_id "
                + "WHERE assignment.attraction_id = :id AND collection.active = TRUE "
                + "ORDER BY assignment.display_order")
        .param("id", id)
        .query(String.class)
        .list();
  }

  private static boolean coordinatesChanged(AttractionRow existing, ValidAttraction valid) {
    return existing.latitude().compareTo(valid.latitude()) != 0
        || existing.longitude().compareTo(valid.longitude()) != 0;
  }

  private static void validateHttpUrl(String value, String error) {
    try {
      URI uri = URI.create(value == null ? "" : value.trim());
      if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
          || uri.getHost() == null) {
        throw badRequest(error);
      }
    } catch (IllegalArgumentException exception) {
      throw badRequest(error);
    }
  }

  private static ResponseStatusException badRequest(String reason) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
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

  public record Translation(
      String language,
      String name,
      String shortDescription,
      String detailedDescription,
      String admissionInformation,
      String practicalInformation) {}

  public record AttractionUpsertRequest(
      String slug,
      BigDecimal latitude,
      BigDecimal longitude,
      String googleMapsUrl,
      Boolean active,
      List<Translation> translations,
      List<String> collectionSlugs) {}

  public record AttractionResponse(
      UUID id,
      String slug,
      BigDecimal latitude,
      BigDecimal longitude,
      String googleMapsUrl,
      boolean active,
      List<Translation> translations,
      List<String> collectionSlugs,
      CalculationSummary distanceCalculation) {}

  public record AttractionPublicResponse(
      String slug,
      String name,
      String shortDescription,
      String detailedDescription,
      String admissionInformation,
      String practicalInformation,
      BigDecimal latitude,
      BigDecimal longitude,
      String googleMapsUrl,
      List<String> collectionSlugs) {}

  public record CollectionResponse(String slug, String name, String shortDescription) {}

  private record AttractionRow(
      UUID id,
      String slug,
      BigDecimal latitude,
      BigDecimal longitude,
      String googleMapsUrl,
      boolean active) {}

  private record PublicAttractionRow(
      UUID id,
      String slug,
      String name,
      String shortDescription,
      String detailedDescription,
      String admissionInformation,
      String practicalInformation,
      BigDecimal latitude,
      BigDecimal longitude,
      String googleMapsUrl) {}

  private record ValidAttraction(
      String slug,
      BigDecimal latitude,
      BigDecimal longitude,
      String googleMapsUrl,
      boolean active,
      Map<String, Translation> translations,
      List<String> collectionSlugs) {}
}
