package com.bukovina.platform.accommodation.amenity.service;

import com.bukovina.platform.accommodation.amenity.dto.AdminAmenityAssignmentResponse;
import com.bukovina.platform.accommodation.amenity.dto.AdminAmenityAssignmentUpdateRequest;
import com.bukovina.platform.accommodation.amenity.dto.AdminAmenityOrderUpdateRequest;
import com.bukovina.platform.accommodation.amenity.dto.AdminAmenityResponse;
import com.bukovina.platform.accommodation.amenity.dto.AdminAmenityTranslationResponse;
import com.bukovina.platform.accommodation.amenity.dto.AdminAmenityTranslationUpdateRequest;
import com.bukovina.platform.accommodation.amenity.dto.AdminAmenityUpdateRequest;
import com.bukovina.platform.accommodation.guesthouse.dao.GuesthouseRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAmenityService {

  private static final List<String> LANGUAGES = List.of("hu", "ro", "en");
  private static final Set<String> CATEGORIES =
      Set.of("ROOM_COMFORT", "FOOD_KITCHEN", "OUTDOOR_WELLNESS", "PROGRAM_GROUP");
  private static final Set<String> PRICING_TYPES = Set.of("FREE", "PAID");

  private final JdbcClient jdbcClient;
  private final GuesthouseRepository guesthouseRepository;

  public AdminAmenityService(JdbcClient jdbcClient, GuesthouseRepository guesthouseRepository) {
    this.jdbcClient = jdbcClient;
    this.guesthouseRepository = guesthouseRepository;
  }

  @Transactional(readOnly = true)
  public List<AdminAmenityResponse> list() {
    return jdbcClient.sql("SELECT id FROM amenity ORDER BY code").query(UUID.class).list().stream()
        .map(this::find)
        .toList();
  }

  @Transactional
  public AdminAmenityResponse create(AdminAmenityUpdateRequest request) {
    ValidAmenity valid = validate(request, null);
    if (codeExists(valid.code(), null)) {
      throw new AdminAmenityException("AMENITY_CODE_ALREADY_EXISTS");
    }

    UUID amenityId = UUID.randomUUID();
    jdbcClient
        .sql(
            "INSERT INTO amenity (id, code, category, pricing_type) VALUES (:id, :code, :category, :pricingType)")
        .param("id", amenityId)
        .param("code", valid.code())
        .param("category", valid.category())
        .param("pricingType", valid.pricingType())
        .update();
    replaceTranslations(amenityId, valid.translations());
    replaceAssignments(amenityId, valid.assignments());
    return find(amenityId);
  }

  @Transactional
  public AdminAmenityResponse update(UUID amenityId, AdminAmenityUpdateRequest request) {
    ensureAmenityExists(amenityId);
    ValidAmenity valid = validate(request, amenityId);
    if (codeExists(valid.code(), amenityId)) {
      throw new AdminAmenityException("AMENITY_CODE_ALREADY_EXISTS");
    }

    jdbcClient
        .sql(
            "UPDATE amenity SET code = :code, category = :category, pricing_type = :pricingType WHERE id = :id")
        .param("id", amenityId)
        .param("code", valid.code())
        .param("category", valid.category())
        .param("pricingType", valid.pricingType())
        .update();
    replaceTranslations(amenityId, valid.translations());
    replaceAssignments(amenityId, valid.assignments());
    return find(amenityId);
  }

  @Transactional
  public void reorder(UUID guesthouseId, AdminAmenityOrderUpdateRequest request) {
    ensureGuesthouseExists(guesthouseId);
    List<UUID> ids = request.amenityIds();
    if (ids.size() != Set.copyOf(ids).size()) {
      throw new AdminAmenityException("INVALID_AMENITY_ORDER");
    }
    List<UUID> assignedIds =
        jdbcClient
            .sql(
                "SELECT amenity_id FROM guesthouse_amenity WHERE guesthouse_id = :guesthouseId ORDER BY display_order")
            .param("guesthouseId", guesthouseId)
            .query(UUID.class)
            .list();
    if (!Set.copyOf(ids).equals(Set.copyOf(assignedIds))) {
      throw new AdminAmenityException("INVALID_AMENITY_ORDER");
    }
    for (int index = 0; index < ids.size(); index++) {
      jdbcClient
          .sql(
              "UPDATE guesthouse_amenity SET display_order = :displayOrder WHERE guesthouse_id = :guesthouseId AND amenity_id = :amenityId")
          .param("guesthouseId", guesthouseId)
          .param("amenityId", ids.get(index))
          .param("displayOrder", index)
          .update();
    }
  }

  private AdminAmenityResponse find(UUID amenityId) {
    AmenityRow amenity =
        jdbcClient
            .sql("SELECT id, code, category, pricing_type FROM amenity WHERE id = :id")
            .param("id", amenityId)
            .query(
                (resultSet, rowNumber) ->
                    new AmenityRow(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("code"),
                        resultSet.getString("category"),
                        resultSet.getString("pricing_type")))
            .optional()
            .orElseThrow(() -> new AdminAmenityException("ADMIN_AMENITY_NOT_FOUND"));
    return new AdminAmenityResponse(
        amenity.id(),
        amenity.code(),
        amenity.category(),
        amenity.pricingType(),
        translationsFor(amenityId),
        assignmentsFor(amenityId));
  }

  private List<AdminAmenityTranslationResponse> translationsFor(UUID amenityId) {
    Map<String, AdminAmenityTranslationResponse> translations = new LinkedHashMap<>();
    jdbcClient
        .sql(
            "SELECT language_code, name, description, detailed_description FROM amenity_translation WHERE amenity_id = :amenityId")
        .param("amenityId", amenityId)
        .query(
            (resultSet, rowNumber) ->
                new AdminAmenityTranslationResponse(
                    resultSet.getString("language_code"),
                    resultSet.getString("name"),
                    nullToEmpty(resultSet.getString("description")),
                    nullToEmpty(resultSet.getString("detailed_description"))))
        .list()
        .forEach(translation -> translations.put(translation.language(), translation));
    return LANGUAGES.stream()
        .map(
            language ->
                translations.getOrDefault(
                    language, new AdminAmenityTranslationResponse(language, "", "", "")))
        .toList();
  }

  private List<AdminAmenityAssignmentResponse> assignmentsFor(UUID amenityId) {
    return jdbcClient
        .sql(
            "SELECT guesthouse_id, active, display_order FROM guesthouse_amenity WHERE amenity_id = :amenityId ORDER BY guesthouse_id")
        .param("amenityId", amenityId)
        .query(
            (resultSet, rowNumber) ->
                new AdminAmenityAssignmentResponse(
                    resultSet.getObject("guesthouse_id", UUID.class),
                    resultSet.getBoolean("active"),
                    resultSet.getInt("display_order")))
        .list();
  }

  private ValidAmenity validate(AdminAmenityUpdateRequest request, UUID amenityId) {
    String code = request.code().trim();
    if (!CATEGORIES.contains(request.category())) {
      throw validation("category", "INVALID_CATEGORY");
    }
    if (!PRICING_TYPES.contains(request.pricingType())) {
      throw validation("pricingType", "INVALID_PRICING_TYPE");
    }

    Map<String, AdminAmenityTranslationUpdateRequest> translations = new LinkedHashMap<>();
    for (AdminAmenityTranslationUpdateRequest translation : request.translations()) {
      if (translation.language() == null || !LANGUAGES.contains(translation.language())) {
        throw validation("translations", "INVALID_LANGUAGE");
      }
      if (translations.put(translation.language(), translation) != null) {
        throw validation("translations", "DUPLICATE_LANGUAGE");
      }
    }
    if (!translations.keySet().equals(Set.copyOf(LANGUAGES))) {
      throw validation("translations", "INCOMPLETE_TRANSLATIONS");
    }
    if (isBlank(translations.get("hu").name())) {
      throw validation("translations.hu.name", "REQUIRED");
    }

    Map<UUID, AdminAmenityAssignmentUpdateRequest> assignments = new LinkedHashMap<>();
    for (AdminAmenityAssignmentUpdateRequest assignment : request.assignments()) {
      if (assignments.put(assignment.guesthouseId(), assignment) != null) {
        throw validation("assignments", "DUPLICATE_GUESTHOUSE");
      }
      ensureGuesthouseExists(assignment.guesthouseId());
    }
    validateDisplayOrders(assignments, amenityId);
    return new ValidAmenity(
        code, request.category(), request.pricingType(), translations, assignments);
  }

  private void validateDisplayOrders(
      Map<UUID, AdminAmenityAssignmentUpdateRequest> assignments, UUID amenityId) {
    for (AdminAmenityAssignmentUpdateRequest assignment : assignments.values()) {
      Integer existing =
          amenityId == null
              ? jdbcClient
                  .sql(
                      "SELECT COUNT(*) FROM guesthouse_amenity WHERE guesthouse_id = :guesthouseId AND display_order = :displayOrder")
                  .param("guesthouseId", assignment.guesthouseId())
                  .param("displayOrder", assignment.displayOrder())
                  .query(Integer.class)
                  .single()
              : jdbcClient
                  .sql(
                      "SELECT COUNT(*) FROM guesthouse_amenity WHERE guesthouse_id = :guesthouseId AND display_order = :displayOrder AND amenity_id <> :amenityId")
                  .param("guesthouseId", assignment.guesthouseId())
                  .param("displayOrder", assignment.displayOrder())
                  .param("amenityId", amenityId)
                  .query(Integer.class)
                  .single();
      if (existing > 0) {
        throw validation("assignments", "DUPLICATE_DISPLAY_ORDER");
      }
    }
  }

  private void replaceTranslations(
      UUID amenityId, Map<String, AdminAmenityTranslationUpdateRequest> translations) {
    jdbcClient
        .sql("DELETE FROM amenity_translation WHERE amenity_id = :amenityId")
        .param("amenityId", amenityId)
        .update();
    for (String language : LANGUAGES) {
      AdminAmenityTranslationUpdateRequest translation = translations.get(language);
      if (!"hu".equals(language) && isBlank(translation.name())) {
        continue;
      }
      jdbcClient
          .sql(
              "INSERT INTO amenity_translation (amenity_id, language_code, name, description, detailed_description) VALUES (:amenityId, :language, :name, :description, :detailedDescription)")
          .param("amenityId", amenityId)
          .param("language", language)
          .param("name", translation.name().trim())
          .param("description", nullIfBlank(translation.description()))
          .param("detailedDescription", nullIfBlank(translation.detailedDescription()))
          .update();
    }
  }

  private void replaceAssignments(
      UUID amenityId, Map<UUID, AdminAmenityAssignmentUpdateRequest> assignments) {
    jdbcClient
        .sql("DELETE FROM guesthouse_amenity WHERE amenity_id = :amenityId")
        .param("amenityId", amenityId)
        .update();
    for (AdminAmenityAssignmentUpdateRequest assignment : assignments.values()) {
      jdbcClient
          .sql(
              "INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order) VALUES (:guesthouseId, :amenityId, :active, :displayOrder)")
          .param("guesthouseId", assignment.guesthouseId())
          .param("amenityId", amenityId)
          .param("active", assignment.active())
          .param("displayOrder", assignment.displayOrder())
          .update();
    }
  }

  private boolean codeExists(String code, UUID amenityId) {
    Integer count =
        amenityId == null
            ? jdbcClient
                .sql("SELECT COUNT(*) FROM amenity WHERE code = :code")
                .param("code", code)
                .query(Integer.class)
                .single()
            : jdbcClient
                .sql("SELECT COUNT(*) FROM amenity WHERE code = :code AND id <> :amenityId")
                .param("code", code)
                .param("amenityId", amenityId)
                .query(Integer.class)
                .single();
    return count > 0;
  }

  private void ensureAmenityExists(UUID amenityId) {
    if (!jdbcClient
        .sql("SELECT EXISTS(SELECT 1 FROM amenity WHERE id = :id)")
        .param("id", amenityId)
        .query(Boolean.class)
        .single()) {
      throw new AdminAmenityException("ADMIN_AMENITY_NOT_FOUND");
    }
  }

  private void ensureGuesthouseExists(UUID guesthouseId) {
    if (!guesthouseRepository.existsById(guesthouseId)) {
      throw new AdminAmenityException("ADMIN_AMENITY_GUESTHOUSE_NOT_FOUND");
    }
  }

  private AdminAmenityException validation(String field, String error) {
    return new AdminAmenityException("ADMIN_AMENITY_VALIDATION_FAILED:" + field + ":" + error);
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static String nullIfBlank(String value) {
    return isBlank(value) ? null : value.trim();
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private record AmenityRow(UUID id, String code, String category, String pricingType) {}

  private record ValidAmenity(
      String code,
      String category,
      String pricingType,
      Map<String, AdminAmenityTranslationUpdateRequest> translations,
      Map<UUID, AdminAmenityAssignmentUpdateRequest> assignments) {}
}
