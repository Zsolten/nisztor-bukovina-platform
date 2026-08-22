package com.bukovina.platform.tourism.activity.service;

import com.bukovina.platform.tourism.activity.dao.AttractionDao;
import com.bukovina.platform.tourism.activity.dto.AttractionPublicResponse;
import com.bukovina.platform.tourism.activity.dto.AttractionResponse;
import com.bukovina.platform.tourism.activity.dto.AttractionTranslation;
import com.bukovina.platform.tourism.activity.dto.AttractionUpsertRequest;
import com.bukovina.platform.tourism.activity.dto.CollectionResponse;
import com.bukovina.platform.tourism.activity.exception.AttractionException;
import com.bukovina.platform.tourism.activity.model.Attraction;
import com.bukovina.platform.tourism.activity.model.AttractionContent;
import com.bukovina.platform.tourism.activity.model.PublicAttraction;
import com.bukovina.platform.tourism.activity.model.ValidatedAttraction;
import com.bukovina.platform.tourism.routing.DrivingDistanceMatrixService;
import com.bukovina.platform.tourism.routing.DrivingDistanceMatrixService.CalculationSummary;
import com.bukovina.platform.tourism.routing.StarTourRouteCacheInvalidator;
import java.math.BigDecimal;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AttractionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AttractionService.class);
  private static final Set<String> LANGUAGES = Set.of("hu", "ro", "en");
  private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
  private static final int MIN_VISIT_MINUTES = 5;
  private static final int MAX_VISIT_MINUTES = 720;
  private final AttractionDao dao;
  private final DrivingDistanceMatrixService drivingDistanceMatrix;
  private final StarTourRouteCacheInvalidator routeCacheInvalidator;
  private final TransactionTemplate transactionTemplate;

  public AttractionService(
      AttractionDao dao,
      DrivingDistanceMatrixService drivingDistanceMatrix,
      StarTourRouteCacheInvalidator routeCacheInvalidator,
      PlatformTransactionManager transactionManager) {
    this.dao = dao;
    this.drivingDistanceMatrix = drivingDistanceMatrix;
    this.routeCacheInvalidator = routeCacheInvalidator;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  @Transactional(readOnly = true)
  public List<AttractionResponse> listAdmin() {
    return dao.findAllIds().stream().map(id -> findAdmin(id, null)).toList();
  }

  public AttractionResponse create(AttractionUpsertRequest request) {
    ValidatedAttraction valid = validate(request, null);
    UUID id = UUID.randomUUID();
    transactionTemplate.executeWithoutResult(
        status -> {
          dao.insert(id, valid);
          dao.replaceChildren(id, valid);
        });
    return findAdmin(id, recalculateDistances(id));
  }

  public AttractionResponse update(UUID id, AttractionUpsertRequest request) {
    Attraction existing = dao.findById(id);
    ValidatedAttraction valid = validate(request, id);
    boolean coordinatesChanged = coordinatesChanged(existing, valid);
    boolean routeInputsChanged = coordinatesChanged || existing.active() != valid.active();
    transactionTemplate.executeWithoutResult(
        status -> {
          dao.update(id, valid);
          dao.replaceChildren(id, valid);
          if (routeInputsChanged) routeCacheInvalidator.invalidateForAttraction(id);
        });
    return findAdmin(id, coordinatesChanged ? recalculateDistances(id) : null);
  }

  @Transactional(readOnly = true)
  public List<AttractionPublicResponse> listPublic(String language) {
    return dao.findAllPublic(language).stream().map(this::toPublicResponse).toList();
  }

  @Transactional(readOnly = true)
  public AttractionPublicResponse getPublic(String slug, String language) {
    return toPublicResponse(dao.findPublicBySlug(slug, language));
  }

  @Transactional(readOnly = true)
  public List<CollectionResponse> listCollections(String language) {
    return dao.findActiveCollections(language).stream()
        .map(row -> new CollectionResponse(row.slug(), row.name(), row.shortDescription()))
        .toList();
  }

  private AttractionResponse findAdmin(UUID id, CalculationSummary calculation) {
    Attraction row = dao.findById(id);
    return new AttractionResponse(
        row.id(),
        row.slug(),
        row.latitude(),
        row.longitude(),
        row.googleMapsUrl(),
        row.recommendedVisitDurationMinutes(),
        row.active(),
        dao.findTranslations(id).stream().map(AttractionService::toDto).toList(),
        dao.findCollectionSlugs(id, false),
        calculation);
  }

  private AttractionPublicResponse toPublicResponse(PublicAttraction row) {
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
        row.recommendedVisitDurationMinutes(),
        dao.findCollectionSlugs(row.id(), true));
  }

  private ValidatedAttraction validate(AttractionUpsertRequest request, UUID currentId) {
    if (request == null || blank(request.slug()) || !SLUG.matcher(request.slug().trim()).matches())
      throw AttractionException.badRequest("INVALID_ATTRACTION_SLUG");
    String slug = request.slug().trim();
    if (dao.slugExists(slug, currentId)) throw AttractionException.slugExists();
    if (request.latitude() == null
        || request.latitude().compareTo(BigDecimal.valueOf(-90)) < 0
        || request.latitude().compareTo(BigDecimal.valueOf(90)) > 0)
      throw AttractionException.badRequest("INVALID_LATITUDE");
    if (request.longitude() == null
        || request.longitude().compareTo(BigDecimal.valueOf(-180)) < 0
        || request.longitude().compareTo(BigDecimal.valueOf(180)) > 0)
      throw AttractionException.badRequest("INVALID_LONGITUDE");
    validateHttpUrl(request.googleMapsUrl(), "INVALID_GOOGLE_MAPS_URL");
    if (request.recommendedVisitDurationMinutes() == null
        || request.recommendedVisitDurationMinutes() < MIN_VISIT_MINUTES
        || request.recommendedVisitDurationMinutes() > MAX_VISIT_MINUTES)
      throw AttractionException.badRequest("INVALID_RECOMMENDED_VISIT_DURATION");
    if (request.active() == null) throw AttractionException.badRequest("ACTIVE_REQUIRED");

    Map<String, AttractionContent> translations = translations(request.translations());
    AttractionContent hu = translations.get("hu");
    if (hu == null
        || blank(hu.name())
        || blank(hu.shortDescription())
        || blank(hu.detailedDescription()))
      throw AttractionException.badRequest("HUNGARIAN_ATTRACTION_CONTENT_REQUIRED");
    List<String> collections =
        request.collectionSlugs() == null
            ? List.of()
            : request.collectionSlugs().stream().map(String::trim).distinct().toList();
    if (collections.stream().anyMatch(value -> !dao.collectionExists(value)))
      throw AttractionException.badRequest("UNKNOWN_TOURISM_COLLECTION");
    return new ValidatedAttraction(
        slug,
        request.latitude(),
        request.longitude(),
        request.googleMapsUrl().trim(),
        request.recommendedVisitDurationMinutes(),
        request.active(),
        translations,
        collections);
  }

  private Map<String, AttractionContent> translations(List<AttractionTranslation> input) {
    if (input == null)
      throw AttractionException.badRequest("HUNGARIAN_ATTRACTION_CONTENT_REQUIRED");
    Map<String, AttractionContent> result = new LinkedHashMap<>();
    for (AttractionTranslation translation : input) {
      if (translation == null
          || !LANGUAGES.contains(translation.language())
          || result.put(translation.language(), toModel(translation)) != null)
        throw AttractionException.badRequest("INVALID_TRANSLATIONS");
    }
    return result;
  }

  private static AttractionContent toModel(AttractionTranslation value) {
    return new AttractionContent(
        value.language(),
        value.name(),
        value.shortDescription(),
        value.detailedDescription(),
        value.admissionInformation(),
        value.practicalInformation());
  }

  private static AttractionTranslation toDto(AttractionContent value) {
    return new AttractionTranslation(
        value.language(),
        value.name(),
        value.shortDescription(),
        value.detailedDescription(),
        value.admissionInformation(),
        value.practicalInformation());
  }

  private CalculationSummary recalculateDistances(UUID attractionId) {
    try {
      return drivingDistanceMatrix.recalculateAffectedPairs(attractionId);
    } catch (RuntimeException exception) {
      LOGGER.warn(
          "Attraction {} was saved, but its driving-distance pairs could not be recalculated",
          attractionId,
          exception);
      return null;
    }
  }

  private static boolean coordinatesChanged(Attraction existing, ValidatedAttraction valid) {
    return existing.latitude().compareTo(valid.latitude()) != 0
        || existing.longitude().compareTo(valid.longitude()) != 0;
  }

  private static void validateHttpUrl(String value, String error) {
    try {
      URI uri = URI.create(value == null ? "" : value.trim());
      if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
          || uri.getHost() == null) throw AttractionException.badRequest(error);
    } catch (IllegalArgumentException exception) {
      throw AttractionException.badRequest(error);
    }
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }
}
