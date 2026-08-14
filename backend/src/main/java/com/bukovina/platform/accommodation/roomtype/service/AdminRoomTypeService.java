package com.bukovina.platform.accommodation.roomtype.service;

import com.bukovina.platform.accommodation.guesthouse.service.GuesthouseExistenceQuery;
import com.bukovina.platform.accommodation.roomtype.dto.AdminRoomTypeOrderUpdateRequest;
import com.bukovina.platform.accommodation.roomtype.dto.AdminRoomTypeResponse;
import com.bukovina.platform.accommodation.roomtype.dto.AdminRoomTypeTranslationResponse;
import com.bukovina.platform.accommodation.roomtype.dto.AdminRoomTypeTranslationUpdateRequest;
import com.bukovina.platform.accommodation.roomtype.dto.AdminRoomTypeUpdateRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminRoomTypeService {

  private static final List<String> LANGUAGES = List.of("hu", "ro", "en");
  private final JdbcClient jdbcClient;
  private final GuesthouseExistenceQuery guesthouseExistenceQuery;

  public AdminRoomTypeService(
      JdbcClient jdbcClient, GuesthouseExistenceQuery guesthouseExistenceQuery) {
    this.jdbcClient = jdbcClient;
    this.guesthouseExistenceQuery = guesthouseExistenceQuery;
  }

  @Transactional(readOnly = true)
  public List<AdminRoomTypeResponse> list(UUID guesthouseId) {
    ensureGuesthouseExists(guesthouseId);
    return roomTypeIds(guesthouseId).stream().map(this::find).toList();
  }

  @Transactional
  public AdminRoomTypeResponse create(UUID guesthouseId, AdminRoomTypeUpdateRequest request) {
    ensureGuesthouseExists(guesthouseId);
    ValidRoomType valid = validate(request, null);
    if (codeExists(guesthouseId, valid.code(), null)) {
      throw new AdminRoomTypeException("ROOM_TYPE_CODE_ALREADY_EXISTS");
    }
    UUID roomTypeId = UUID.randomUUID();
    jdbcClient
        .sql(
            "INSERT INTO room_type (id, guesthouse_id, code, quantity, standard_occupancy, "
                + "rooms_with_extra_bed, extra_beds_per_eligible_room, active, display_order) "
                + "VALUES (:id, :guesthouseId, :code, :quantity, :standardOccupancy, "
                + ":roomsWithExtraBed, :extraBedsPerEligibleRoom, :active, :displayOrder)")
        .param("id", roomTypeId)
        .param("guesthouseId", guesthouseId)
        .param("code", valid.code())
        .param("quantity", valid.quantity())
        .param("standardOccupancy", valid.standardOccupancy())
        .param("roomsWithExtraBed", valid.roomsWithExtraBed())
        .param("extraBedsPerEligibleRoom", valid.extraBedsPerEligibleRoom())
        .param("active", valid.active())
        .param("displayOrder", nextDisplayOrder(guesthouseId))
        .update();
    replaceTranslations(roomTypeId, valid.translations());
    return find(roomTypeId);
  }

  @Transactional
  public AdminRoomTypeResponse update(
      UUID guesthouseId, UUID roomTypeId, AdminRoomTypeUpdateRequest request) {
    ensureGuesthouseExists(guesthouseId);
    RoomTypeRow existing = findRow(roomTypeId);
    if (!existing.guesthouseId().equals(guesthouseId)) {
      throw new AdminRoomTypeException("ROOM_TYPE_NOT_FOUND");
    }
    ValidRoomType valid = validate(request, roomTypeId);
    if (!existing.code().equals(valid.code())) {
      throw validation("code", "IMMUTABLE");
    }
    jdbcClient
        .sql(
            "UPDATE room_type SET quantity = :quantity, standard_occupancy = :standardOccupancy, "
                + "rooms_with_extra_bed = :roomsWithExtraBed, "
                + "extra_beds_per_eligible_room = :extraBedsPerEligibleRoom, active = :active "
                + "WHERE id = :id")
        .param("id", roomTypeId)
        .param("quantity", valid.quantity())
        .param("standardOccupancy", valid.standardOccupancy())
        .param("roomsWithExtraBed", valid.roomsWithExtraBed())
        .param("extraBedsPerEligibleRoom", valid.extraBedsPerEligibleRoom())
        .param("active", valid.active())
        .update();
    replaceTranslations(roomTypeId, valid.translations());
    return find(roomTypeId);
  }

  @Transactional
  public void reorder(UUID guesthouseId, AdminRoomTypeOrderUpdateRequest request) {
    ensureGuesthouseExists(guesthouseId);
    List<UUID> roomTypeIds = request.roomTypeIds();
    if (roomTypeIds.size() != Set.copyOf(roomTypeIds).size()
        || !Set.copyOf(roomTypeIds).equals(Set.copyOf(roomTypeIds(guesthouseId)))) {
      throw new AdminRoomTypeException("INVALID_ROOM_TYPE_ORDER");
    }
    for (int index = 0; index < roomTypeIds.size(); index++) {
      jdbcClient
          .sql("UPDATE room_type SET display_order = :displayOrder WHERE id = :id")
          .param("displayOrder", index)
          .param("id", roomTypeIds.get(index))
          .update();
    }
  }

  private AdminRoomTypeResponse find(UUID roomTypeId) {
    RoomTypeRow roomType = findRow(roomTypeId);
    return new AdminRoomTypeResponse(
        roomType.id(),
        roomType.code(),
        roomType.quantity(),
        roomType.standardOccupancy(),
        roomType.roomsWithExtraBed(),
        roomType.extraBedsPerEligibleRoom(),
        roomType.active(),
        roomType.displayOrder(),
        translationsFor(roomTypeId));
  }

  private RoomTypeRow findRow(UUID roomTypeId) {
    return jdbcClient
        .sql(
            "SELECT id, guesthouse_id, code, quantity, standard_occupancy, rooms_with_extra_bed, "
                + "extra_beds_per_eligible_room, active, display_order FROM room_type WHERE id = :id")
        .param("id", roomTypeId)
        .query(
            (resultSet, rowNumber) ->
                new RoomTypeRow(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getObject("guesthouse_id", UUID.class),
                    resultSet.getString("code"),
                    resultSet.getInt("quantity"),
                    resultSet.getInt("standard_occupancy"),
                    resultSet.getInt("rooms_with_extra_bed"),
                    resultSet.getInt("extra_beds_per_eligible_room"),
                    resultSet.getBoolean("active"),
                    resultSet.getInt("display_order")))
        .optional()
        .orElseThrow(() -> new AdminRoomTypeException("ROOM_TYPE_NOT_FOUND"));
  }

  private List<UUID> roomTypeIds(UUID guesthouseId) {
    return jdbcClient
        .sql(
            "SELECT id FROM room_type WHERE guesthouse_id = :guesthouseId ORDER BY display_order, id")
        .param("guesthouseId", guesthouseId)
        .query(UUID.class)
        .list();
  }

  private List<AdminRoomTypeTranslationResponse> translationsFor(UUID roomTypeId) {
    Map<String, AdminRoomTypeTranslationResponse> translations = new LinkedHashMap<>();
    jdbcClient
        .sql(
            "SELECT language_code, name, short_description, detailed_description "
                + "FROM room_type_translation WHERE room_type_id = :roomTypeId")
        .param("roomTypeId", roomTypeId)
        .query(
            (resultSet, rowNumber) ->
                new AdminRoomTypeTranslationResponse(
                    resultSet.getString("language_code"),
                    resultSet.getString("name"),
                    resultSet.getString("short_description"),
                    nullToEmpty(resultSet.getString("detailed_description"))))
        .list()
        .forEach(translation -> translations.put(translation.language(), translation));
    return LANGUAGES.stream()
        .map(
            language ->
                translations.getOrDefault(
                    language, new AdminRoomTypeTranslationResponse(language, "", "", "")))
        .toList();
  }

  private ValidRoomType validate(AdminRoomTypeUpdateRequest request, UUID roomTypeId) {
    Map<String, AdminRoomTypeTranslationUpdateRequest> translations = new LinkedHashMap<>();
    for (AdminRoomTypeTranslationUpdateRequest translation : request.translations()) {
      String language = translation.language().trim();
      if (!LANGUAGES.contains(language)) {
        throw validation("translations", "INVALID_LANGUAGE");
      }
      if (translations.put(language, translation) != null) {
        throw validation("translations", "DUPLICATE_LANGUAGE");
      }
    }
    if (!translations.keySet().equals(Set.copyOf(LANGUAGES))) {
      throw validation("translations", "INCOMPLETE_TRANSLATIONS");
    }
    AdminRoomTypeTranslationUpdateRequest hungarian = translations.get("hu");
    if (blank(hungarian.name())) {
      throw validation("translations.hu.name", "REQUIRED");
    }
    if (blank(hungarian.shortDescription())) {
      throw validation("translations.hu.shortDescription", "REQUIRED");
    }
    if (request.roomsWithExtraBed() > request.quantity()) {
      throw validation("roomsWithExtraBed", "EXCEEDS_QUANTITY");
    }
    if (request.roomsWithExtraBed() == 0 && request.extraBedsPerEligibleRoom() != 0) {
      throw validation("extraBedsPerEligibleRoom", "NO_ELIGIBLE_ROOM");
    }
    if (request.roomsWithExtraBed() > 0 && request.extraBedsPerEligibleRoom() == 0) {
      throw validation("extraBedsPerEligibleRoom", "REQUIRED");
    }
    return new ValidRoomType(
        request.code().trim(),
        request.quantity(),
        request.standardOccupancy(),
        request.roomsWithExtraBed(),
        request.extraBedsPerEligibleRoom(),
        request.active(),
        translations);
  }

  private void replaceTranslations(
      UUID roomTypeId, Map<String, AdminRoomTypeTranslationUpdateRequest> translations) {
    jdbcClient
        .sql("DELETE FROM room_type_translation WHERE room_type_id = :roomTypeId")
        .param("roomTypeId", roomTypeId)
        .update();
    for (String language : LANGUAGES) {
      AdminRoomTypeTranslationUpdateRequest translation = translations.get(language);
      if (!"hu".equals(language) && blank(translation.name())) {
        continue;
      }
      jdbcClient
          .sql(
              "INSERT INTO room_type_translation (room_type_id, language_code, name, short_description, detailed_description) "
                  + "VALUES (:roomTypeId, :language, :name, :shortDescription, :detailedDescription)")
          .param("roomTypeId", roomTypeId)
          .param("language", language)
          .param("name", translation.name().trim())
          .param("shortDescription", translation.shortDescription().trim())
          .param("detailedDescription", nullIfBlank(translation.detailedDescription()))
          .update();
    }
  }

  private boolean codeExists(UUID guesthouseId, String code, UUID roomTypeId) {
    Integer count =
        roomTypeId == null
            ? jdbcClient
                .sql(
                    "SELECT COUNT(*) FROM room_type WHERE guesthouse_id = :guesthouseId AND code = :code")
                .param("guesthouseId", guesthouseId)
                .param("code", code)
                .query(Integer.class)
                .single()
            : jdbcClient
                .sql(
                    "SELECT COUNT(*) FROM room_type WHERE guesthouse_id = :guesthouseId "
                        + "AND code = :code AND id <> :roomTypeId")
                .param("guesthouseId", guesthouseId)
                .param("code", code)
                .param("roomTypeId", roomTypeId)
                .query(Integer.class)
                .single();
    return count > 0;
  }

  private int nextDisplayOrder(UUID guesthouseId) {
    Integer count =
        jdbcClient
            .sql("SELECT COUNT(*) FROM room_type WHERE guesthouse_id = :guesthouseId")
            .param("guesthouseId", guesthouseId)
            .query(Integer.class)
            .single();
    return count;
  }

  private void ensureGuesthouseExists(UUID guesthouseId) {
    if (!guesthouseExistenceQuery.exists(guesthouseId)) {
      throw new AdminRoomTypeException("ADMIN_ROOM_TYPE_GUESTHOUSE_NOT_FOUND");
    }
  }

  private AdminRoomTypeException validation(String field, String rule) {
    return new AdminRoomTypeException("ADMIN_ROOM_TYPE_VALIDATION_FAILED:" + field + ":" + rule);
  }

  private boolean blank(String value) {
    return value == null || value.isBlank();
  }

  private String nullIfBlank(String value) {
    return blank(value) ? null : value.trim();
  }

  private String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private record ValidRoomType(
      String code,
      int quantity,
      int standardOccupancy,
      int roomsWithExtraBed,
      int extraBedsPerEligibleRoom,
      boolean active,
      Map<String, AdminRoomTypeTranslationUpdateRequest> translations) {}

  private record RoomTypeRow(
      UUID id,
      UUID guesthouseId,
      String code,
      int quantity,
      int standardOccupancy,
      int roomsWithExtraBed,
      int extraBedsPerEligibleRoom,
      boolean active,
      int displayOrder) {}
}
