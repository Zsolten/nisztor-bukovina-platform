package com.bukovina.platform.tourism.startour.service;

import com.bukovina.platform.tourism.startour.service.StarTourRouteService.RouteStatus;
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
public class StarTourService {
  private static final Set<String> LANGUAGES = Set.of("hu", "ro", "en");
  private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
  private static final Pattern COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");
  private final JdbcClient jdbc;
  private final StarTourRouteService routeService;
  private final StarTourStopService stopService;

  public StarTourService(
      JdbcClient jdbc, StarTourRouteService routeService, StarTourStopService stopService) {
    this.jdbc = jdbc;
    this.routeService = routeService;
    this.stopService = stopService;
  }

  @Transactional(readOnly = true)
  public List<StarTourResponse> listAdmin() {
    return jdbc
        .sql("SELECT id FROM star_tour ORDER BY updated_at DESC, slug")
        .query(UUID.class)
        .list()
        .stream()
        .map(this::findAdmin)
        .toList();
  }

  @Transactional
  public StarTourResponse create(StarTourUpsertRequest request) {
    ValidStarTour valid = validate(request, null);
    if (valid.published()) {
      throw badRequest("STAR_TOUR_STOPS_REQUIRED");
    }
    UUID id = UUID.randomUUID();
    jdbc.sql(
            "INSERT INTO star_tour (id, slug, map_color, published, active) "
                + "VALUES (:id, :slug, :color, :published, :active)")
        .param("id", id)
        .param("slug", valid.slug())
        .param("color", valid.mapColor())
        .param("published", valid.published())
        .param("active", valid.active())
        .update();
    replaceChildren(id, valid);
    return findAdmin(id);
  }

  @Transactional
  public StarTourResponse update(UUID id, StarTourUpsertRequest request) {
    boolean wasPublished = publishedState(id);
    ValidStarTour valid = validate(request, id);
    if (!wasPublished && valid.published()) {
      stopService.requirePublishable(id);
    }
    jdbc.sql(
            "UPDATE star_tour SET slug = :slug, map_color = :color, published = :published, "
                + "active = :active, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
        .param("id", id)
        .param("slug", valid.slug())
        .param("color", valid.mapColor())
        .param("published", valid.published())
        .param("active", valid.active())
        .update();
    replaceChildren(id, valid);
    return findAdmin(id);
  }

  @Transactional(readOnly = true)
  public StarTourResponse getAdmin(UUID id) {
    return findAdmin(id);
  }

  @Transactional(readOnly = true)
  public List<StarTourPublicResponse> listPublic(String language) {
    return jdbc
        .sql(publicSelect() + " WHERE tour.published = TRUE AND tour.active = TRUE ORDER BY name")
        .param("language", language)
        .query(this::mapPublicRow)
        .list()
        .stream()
        .map(row -> toPublicResponse(row, language))
        .toList();
  }

  @Transactional(readOnly = true)
  public StarTourPublicResponse getPublic(String slug, String language) {
    return jdbc.sql(
            publicSelect()
                + " WHERE tour.published = TRUE AND tour.active = TRUE AND tour.slug = :slug")
        .param("language", language)
        .param("slug", slug)
        .query(this::mapPublicRow)
        .optional()
        .map(row -> toPublicResponse(row, language))
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "STAR_TOUR_NOT_FOUND"));
  }

  private StarTourResponse findAdmin(UUID id) {
    TourRow row =
        jdbc.sql("SELECT id, slug, map_color, published, active FROM star_tour WHERE id = :id")
            .param("id", id)
            .query(
                (rs, n) ->
                    new TourRow(
                        rs.getObject("id", UUID.class),
                        rs.getString("slug"),
                        rs.getString("map_color"),
                        rs.getBoolean("published"),
                        rs.getBoolean("active")))
            .optional()
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "STAR_TOUR_NOT_FOUND"));
    return new StarTourResponse(
        row.id(),
        row.slug(),
        row.mapColor(),
        row.published(),
        row.active(),
        translationsFor(id),
        tagsFor(id),
        adminImagesFor(id),
        stopService.totalsFor(id),
        routeService.statusFor(id),
        routeService.failureReasonFor(id));
  }

  private ValidStarTour validate(StarTourUpsertRequest request, UUID currentId) {
    if (request == null
        || blank(request.slug())
        || !SLUG.matcher(request.slug().trim()).matches()) {
      throw badRequest("INVALID_STAR_TOUR_SLUG");
    }
    String slug = request.slug().trim();
    Integer duplicate =
        currentId == null
            ? jdbc.sql("SELECT COUNT(*) FROM star_tour WHERE slug = :slug")
                .param("slug", slug)
                .query(Integer.class)
                .single()
            : jdbc.sql("SELECT COUNT(*) FROM star_tour WHERE slug = :slug AND id <> :id")
                .param("slug", slug)
                .param("id", currentId)
                .query(Integer.class)
                .single();
    if (duplicate > 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "STAR_TOUR_SLUG_EXISTS");
    }
    if (blank(request.mapColor()) || !COLOR.matcher(request.mapColor()).matches()) {
      throw badRequest("INVALID_MAP_COLOR");
    }
    if (request.published() == null) {
      throw badRequest("PUBLISHED_REQUIRED");
    }
    if (request.active() == null) {
      throw badRequest("ACTIVE_REQUIRED");
    }

    Map<String, Translation> translations = new LinkedHashMap<>();
    if (request.translations() != null) {
      for (Translation translation : request.translations()) {
        if (translation == null
            || !LANGUAGES.contains(translation.language())
            || translations.put(translation.language(), translation) != null) {
          throw badRequest("INVALID_TRANSLATIONS");
        }
      }
    }
    Translation hu = translations.get("hu");
    if (hu == null
        || blank(hu.name())
        || blank(hu.shortDescription())
        || blank(hu.detailedDescription())) {
      throw badRequest("HUNGARIAN_STAR_TOUR_CONTENT_REQUIRED");
    }

    List<String> tags =
        request.tags() == null
            ? List.of()
            : request.tags().stream()
                .filter(tag -> !blank(tag))
                .map(String::trim)
                .distinct()
                .toList();
    if (tags.stream().anyMatch(tag -> tag.length() > 80)) {
      throw badRequest("INVALID_STAR_TOUR_TAG");
    }
    List<Image> images = request.images() == null ? List.of() : request.images();
    for (Image image : images) {
      if (image == null || blank(image.imageUrl())) {
        throw badRequest("INVALID_STAR_TOUR_IMAGE");
      }
      validateHttpUrl(image.imageUrl());
    }
    return new ValidStarTour(
        slug,
        request.mapColor().toUpperCase(),
        request.published(),
        request.active(),
        translations,
        tags,
        images);
  }

  private void replaceChildren(UUID id, ValidStarTour valid) {
    jdbc.sql("DELETE FROM star_tour_translation WHERE star_tour_id = :id").param("id", id).update();
    for (Translation translation : valid.translations().values()) {
      if (!"hu".equals(translation.language()) && blank(translation.name())) {
        continue;
      }
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
    for (String tag : valid.tags()) {
      jdbc.sql("INSERT INTO star_tour_tag (star_tour_id, tag) VALUES (:id, :tag)")
          .param("id", id)
          .param("tag", tag)
          .update();
    }
    jdbc.sql("DELETE FROM star_tour_image WHERE star_tour_id = :id").param("id", id).update();
    for (int index = 0; index < valid.images().size(); index++) {
      Image image = valid.images().get(index);
      UUID imageId = UUID.randomUUID();
      jdbc.sql(
              "INSERT INTO star_tour_image (id, star_tour_id, image_url, display_order) VALUES (:imageId, :id, :url, :position)")
          .param("imageId", imageId)
          .param("id", id)
          .param("url", image.imageUrl().trim())
          .param("position", index)
          .update();
      if (!blank(image.altText())) {
        jdbc.sql(
                "INSERT INTO star_tour_image_translation (image_id, language_code, alt_text) "
                    + "VALUES (:imageId, 'hu', :altText)")
            .param("imageId", imageId)
            .param("altText", image.altText().trim())
            .update();
      }
    }
  }

  private List<Translation> translationsFor(UUID id) {
    return jdbc.sql(
            "SELECT language_code, name, short_description, detailed_description FROM star_tour_translation "
                + "WHERE star_tour_id = :id ORDER BY language_code")
        .param("id", id)
        .query(
            (rs, n) ->
                new Translation(
                    rs.getString("language_code"),
                    rs.getString("name"),
                    rs.getString("short_description"),
                    rs.getString("detailed_description")))
        .list();
  }

  private List<String> tagsFor(UUID id) {
    return jdbc.sql("SELECT tag FROM star_tour_tag WHERE star_tour_id = :id ORDER BY tag")
        .param("id", id)
        .query(String.class)
        .list();
  }

  private List<Image> imagesFor(UUID id, String language) {
    return jdbc.sql(
            "SELECT image.image_url, requested.alt_text "
                + "FROM star_tour_image image JOIN star_tour_image_translation requested "
                + "ON requested.image_id = image.id AND requested.language_code = :language "
                + "WHERE image.star_tour_id = :id ORDER BY image.display_order")
        .param("language", language)
        .param("id", id)
        .query((rs, n) -> new Image(rs.getString("image_url"), rs.getString("alt_text")))
        .list();
  }

  private List<Image> adminImagesFor(UUID id) {
    return jdbc.sql(
            "SELECT image.image_url, COALESCE(hu.alt_text, '') alt_text "
                + "FROM star_tour_image image LEFT JOIN star_tour_image_translation hu "
                + "ON hu.image_id = image.id AND hu.language_code = 'hu' "
                + "WHERE image.star_tour_id = :id ORDER BY image.display_order")
        .param("id", id)
        .query((rs, n) -> new Image(rs.getString("image_url"), rs.getString("alt_text")))
        .list();
  }

  private String publicSelect() {
    return "SELECT tour.id, tour.slug, tour.map_color, requested.name, "
        + "requested.short_description, requested.detailed_description "
        + "FROM star_tour tour JOIN star_tour_translation requested ON requested.star_tour_id = tour.id "
        + "AND requested.language_code = :language";
  }

  private PublicTourRow mapPublicRow(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
    return new PublicTourRow(
        rs.getObject("id", UUID.class),
        rs.getString("slug"),
        rs.getString("name"),
        rs.getString("short_description"),
        rs.getString("detailed_description"),
        rs.getString("map_color"));
  }

  private StarTourPublicResponse toPublicResponse(PublicTourRow row, String language) {
    return new StarTourPublicResponse(
        row.slug(),
        row.name(),
        row.shortDescription(),
        row.detailedDescription(),
        row.mapColor(),
        tagsFor(row.id()),
        imagesFor(row.id(), language),
        stopsFor(row.id(), language),
        stopService.totalsFor(row.id()),
        routeService.statusFor(row.id()));
  }

  private List<Stop> stopsFor(UUID id, String language) {
    return jdbc.sql(
            "SELECT attraction.slug, translation.name, attraction.latitude, attraction.longitude, "
                + "attraction.google_maps_url, assignment.optional_stop, "
                + "COALESCE(assignment.planned_visit_duration_minutes, attraction.recommended_visit_duration_minutes) "
                + "AS visit_duration_minutes "
                + "FROM star_tour_attraction assignment "
                + "JOIN attraction ON attraction.id = assignment.attraction_id AND attraction.active = TRUE "
                + "JOIN attraction_translation translation ON translation.attraction_id = attraction.id "
                + "AND translation.language_code = :language "
                + "WHERE assignment.star_tour_id = :id ORDER BY assignment.display_order")
        .param("language", language)
        .param("id", id)
        .query(
            (rs, row) ->
                new Stop(
                    rs.getString("slug"),
                    rs.getString("name"),
                    rs.getBigDecimal("latitude"),
                    rs.getBigDecimal("longitude"),
                    rs.getString("google_maps_url"),
                    rs.getBoolean("optional_stop"),
                    rs.getInt("visit_duration_minutes")))
        .list();
  }

  private boolean publishedState(UUID id) {
    return jdbc.sql("SELECT published FROM star_tour WHERE id = :id")
        .param("id", id)
        .query(Boolean.class)
        .optional()
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "STAR_TOUR_NOT_FOUND"));
  }

  private static void validateHttpUrl(String value) {
    try {
      URI uri = URI.create(value.trim());
      if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
          || uri.getHost() == null) {
        throw badRequest("INVALID_STAR_TOUR_IMAGE");
      }
    } catch (IllegalArgumentException exception) {
      throw badRequest("INVALID_STAR_TOUR_IMAGE");
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

  public record Translation(
      String language, String name, String shortDescription, String detailedDescription) {}

  public record Image(String imageUrl, String altText) {}

  public record Stop(
      String slug,
      String name,
      java.math.BigDecimal latitude,
      java.math.BigDecimal longitude,
      String googleMapsUrl,
      boolean optional,
      int visitDurationMinutes) {}

  public record StarTourUpsertRequest(
      String slug,
      String mapColor,
      Boolean published,
      Boolean active,
      List<Translation> translations,
      List<String> tags,
      List<Image> images) {}

  public record StarTourResponse(
      UUID id,
      String slug,
      String mapColor,
      boolean published,
      boolean active,
      List<Translation> translations,
      List<String> tags,
      List<Image> images,
      StarTourStopService.TourTotals totals,
      RouteStatus routeStatus,
      String routeFailureReason) {}

  public record StarTourPublicResponse(
      String slug,
      String name,
      String shortDescription,
      String detailedDescription,
      String mapColor,
      List<String> tags,
      List<Image> images,
      List<Stop> stops,
      StarTourStopService.TourTotals totals,
      RouteStatus routeStatus) {}

  private record TourRow(
      UUID id, String slug, String mapColor, boolean published, boolean active) {}

  private record PublicTourRow(
      UUID id,
      String slug,
      String name,
      String shortDescription,
      String detailedDescription,
      String mapColor) {}

  private record ValidStarTour(
      String slug,
      String mapColor,
      boolean published,
      boolean active,
      Map<String, Translation> translations,
      List<String> tags,
      List<Image> images) {}
}
