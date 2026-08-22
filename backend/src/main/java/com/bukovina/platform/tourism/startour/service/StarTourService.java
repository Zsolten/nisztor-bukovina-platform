package com.bukovina.platform.tourism.startour.service;

import com.bukovina.platform.tourism.startour.dao.StarTourDao;
import com.bukovina.platform.tourism.startour.dto.StarTourImage;
import com.bukovina.platform.tourism.startour.dto.StarTourPublicResponse;
import com.bukovina.platform.tourism.startour.dto.StarTourResponse;
import com.bukovina.platform.tourism.startour.dto.StarTourStop;
import com.bukovina.platform.tourism.startour.dto.StarTourTranslation;
import com.bukovina.platform.tourism.startour.dto.StarTourUpsertRequest;
import com.bukovina.platform.tourism.startour.exception.StarTourException;
import com.bukovina.platform.tourism.startour.model.PublicStarTour;
import com.bukovina.platform.tourism.startour.model.PublicTourStop;
import com.bukovina.platform.tourism.startour.model.StarTour;
import com.bukovina.platform.tourism.startour.model.StarTourContent;
import com.bukovina.platform.tourism.startour.model.StarTourImageData;
import com.bukovina.platform.tourism.startour.model.ValidatedStarTour;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StarTourService {
  private static final Set<String> LANGUAGES = Set.of("hu", "ro", "en");
  private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
  private static final Pattern COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");
  private final StarTourDao dao;
  private final StarTourRouteService routeService;
  private final StarTourStopService stopService;

  public StarTourService(
      StarTourDao dao, StarTourRouteService routeService, StarTourStopService stopService) {
    this.dao = dao;
    this.routeService = routeService;
    this.stopService = stopService;
  }

  @Transactional(readOnly = true)
  public List<StarTourResponse> listAdmin() {
    return dao.findAllIds().stream().map(this::findAdmin).toList();
  }

  @Transactional
  public StarTourResponse create(StarTourUpsertRequest request) {
    ValidatedStarTour valid = validate(request, null);
    if (valid.published()) throw StarTourException.badRequest("STAR_TOUR_STOPS_REQUIRED");
    UUID id = UUID.randomUUID();
    dao.insert(id, valid);
    dao.replaceChildren(id, valid);
    return findAdmin(id);
  }

  @Transactional
  public StarTourResponse update(UUID id, StarTourUpsertRequest request) {
    boolean wasPublished = dao.publishedState(id);
    ValidatedStarTour valid = validate(request, id);
    if (!wasPublished && valid.published()) stopService.requirePublishable(id);
    dao.update(id, valid);
    dao.replaceChildren(id, valid);
    return findAdmin(id);
  }

  @Transactional(readOnly = true)
  public StarTourResponse getAdmin(UUID id) {
    return findAdmin(id);
  }

  @Transactional(readOnly = true)
  public List<StarTourPublicResponse> listPublic(String language) {
    return dao.findAllPublic(language).stream()
        .map(row -> toPublicResponse(row, language))
        .toList();
  }

  @Transactional(readOnly = true)
  public StarTourPublicResponse getPublic(String slug, String language) {
    return toPublicResponse(dao.findPublicBySlug(slug, language), language);
  }

  private StarTourResponse findAdmin(UUID id) {
    StarTour row = dao.findById(id);
    return new StarTourResponse(
        row.id(),
        row.slug(),
        row.mapColor(),
        row.published(),
        row.active(),
        dao.findTranslations(id).stream().map(StarTourService::toDto).toList(),
        dao.findTags(id),
        dao.findAdminImages(id).stream().map(StarTourService::toDto).toList(),
        stopService.totalsFor(id),
        routeService.statusFor(id),
        routeService.failureReasonFor(id));
  }

  private StarTourPublicResponse toPublicResponse(PublicStarTour row, String language) {
    return new StarTourPublicResponse(
        row.slug(),
        row.name(),
        row.shortDescription(),
        row.detailedDescription(),
        row.mapColor(),
        dao.findTags(row.id()),
        dao.findImages(row.id(), language).stream().map(StarTourService::toDto).toList(),
        dao.findStops(row.id(), language).stream().map(StarTourService::toDto).toList(),
        stopService.totalsFor(row.id()),
        routeService.statusFor(row.id()));
  }

  private ValidatedStarTour validate(StarTourUpsertRequest request, UUID currentId) {
    if (request == null || blank(request.slug()) || !SLUG.matcher(request.slug().trim()).matches())
      throw StarTourException.badRequest("INVALID_STAR_TOUR_SLUG");
    String slug = request.slug().trim();
    if (dao.slugExists(slug, currentId)) throw StarTourException.slugExists();
    if (blank(request.mapColor()) || !COLOR.matcher(request.mapColor()).matches())
      throw StarTourException.badRequest("INVALID_MAP_COLOR");
    if (request.published() == null) throw StarTourException.badRequest("PUBLISHED_REQUIRED");
    if (request.active() == null) throw StarTourException.badRequest("ACTIVE_REQUIRED");
    Map<String, StarTourContent> translations = new LinkedHashMap<>();
    if (request.translations() != null)
      for (StarTourTranslation translation : request.translations()) {
        if (translation == null
            || !LANGUAGES.contains(translation.language())
            || translations.put(translation.language(), toModel(translation)) != null)
          throw StarTourException.badRequest("INVALID_TRANSLATIONS");
      }
    StarTourContent hu = translations.get("hu");
    if (hu == null
        || blank(hu.name())
        || blank(hu.shortDescription())
        || blank(hu.detailedDescription()))
      throw StarTourException.badRequest("HUNGARIAN_STAR_TOUR_CONTENT_REQUIRED");
    List<String> tags =
        request.tags() == null
            ? List.of()
            : request.tags().stream()
                .filter(tag -> !blank(tag))
                .map(String::trim)
                .distinct()
                .toList();
    if (tags.stream().anyMatch(tag -> tag.length() > 80))
      throw StarTourException.badRequest("INVALID_STAR_TOUR_TAG");
    List<StarTourImage> requestedImages = request.images() == null ? List.of() : request.images();
    for (StarTourImage image : requestedImages) {
      if (image == null || blank(image.imageUrl()))
        throw StarTourException.badRequest("INVALID_STAR_TOUR_IMAGE");
      validateHttpUrl(image.imageUrl());
    }
    return new ValidatedStarTour(
        slug,
        request.mapColor().toUpperCase(),
        request.published(),
        request.active(),
        translations,
        tags,
        requestedImages.stream().map(StarTourService::toModel).toList());
  }

  private static StarTourContent toModel(StarTourTranslation value) {
    return new StarTourContent(
        value.language(), value.name(), value.shortDescription(), value.detailedDescription());
  }

  private static StarTourTranslation toDto(StarTourContent value) {
    return new StarTourTranslation(
        value.language(), value.name(), value.shortDescription(), value.detailedDescription());
  }

  private static StarTourImageData toModel(StarTourImage value) {
    return new StarTourImageData(value.imageUrl(), value.altText());
  }

  private static StarTourImage toDto(StarTourImageData value) {
    return new StarTourImage(value.imageUrl(), value.altText());
  }

  private static StarTourStop toDto(PublicTourStop value) {
    return new StarTourStop(
        value.slug(),
        value.name(),
        value.latitude(),
        value.longitude(),
        value.googleMapsUrl(),
        value.optional(),
        value.visitDurationMinutes());
  }

  private static void validateHttpUrl(String value) {
    try {
      URI uri = URI.create(value.trim());
      if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
          || uri.getHost() == null) throw StarTourException.badRequest("INVALID_STAR_TOUR_IMAGE");
    } catch (IllegalArgumentException exception) {
      throw StarTourException.badRequest("INVALID_STAR_TOUR_IMAGE");
    }
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }
}
