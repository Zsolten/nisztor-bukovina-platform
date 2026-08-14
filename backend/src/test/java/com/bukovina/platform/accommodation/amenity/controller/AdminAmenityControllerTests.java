package com.bukovina.platform.accommodation.amenity.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import(PostgreSqlTestContainerConfiguration.class)
@Transactional
class AdminAmenityControllerTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void protectsTheServiceCatalogueFromAnonymousUsers() throws Exception {
    mockMvc.perform(get("/api/admin/amenities")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createsSharedServiceWithHungarianFallbackAndControlsPublicVisibility() throws Exception {
    UUID nisztor = guesthouseId("nisztor-panzio");
    UUID bukovina = guesthouseId("bukovina-panzio");
    int nisztorOrder = nextOrder(nisztor);
    int bukovinaOrder = nextOrder(bukovina);

    mockMvc
        .perform(
            post("/api/admin/amenities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    request(
                        "table_tennis",
                        "PROGRAM_GROUP",
                        "FREE",
                        nisztor,
                        nisztorOrder,
                        bukovina,
                        bukovinaOrder,
                        true)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value("table_tennis"))
        .andExpect(jsonPath("$.pricingType").value("FREE"))
        .andExpect(jsonPath("$.assignments.length()").value(2));

    mockMvc
        .perform(get("/api/guesthouses/nisztor-panzio").param("lang", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.amenities[?(@.id == 'table_tennis')].name").value("Asztalitenisz"))
        .andExpect(jsonPath("$.amenities[?(@.id == 'table_tennis')].pricingType").value("FREE"));

    UUID amenityId = amenityId("table_tennis");
    mockMvc
        .perform(
            put("/api/admin/amenities/{amenityId}", amenityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    request(
                        "table_tennis",
                        "PROGRAM_GROUP",
                        "PAID",
                        nisztor,
                        nisztorOrder,
                        bukovina,
                        bukovinaOrder,
                        false)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pricingType").value("PAID"));

    mockMvc
        .perform(get("/api/guesthouses/nisztor-panzio").param("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.amenities[?(@.id == 'table_tennis')]").isEmpty());
    mockMvc
        .perform(get("/api/guesthouses/bukovina-panzio").param("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.amenities[?(@.id == 'table_tennis')].pricingType").value("PAID"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void rejectsInvalidServiceDataAndAcceptsACompleteReorder() throws Exception {
    UUID nisztor = guesthouseId("nisztor-panzio");
    int order = nextOrder(nisztor);

    mockMvc
        .perform(
            post("/api/admin/amenities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    request("bad_category", "INVALID", "FREE", nisztor, order, null, null, true)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ADMIN_AMENITY_VALIDATION_FAILED"))
        .andExpect(jsonPath("$.fieldErrors.category").value("INVALID_CATEGORY"));

    mockMvc
        .perform(
            post("/api/admin/amenities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    request(
                        "badminton", "PROGRAM_GROUP", "FREE", nisztor, order, null, null, true)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/admin/amenities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    request(
                        "badminton",
                        "PROGRAM_GROUP",
                        "FREE",
                        nisztor,
                        order + 1,
                        null,
                        null,
                        true)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("AMENITY_CODE_ALREADY_EXISTS"));

    List<UUID> ids =
        jdbcTemplate.queryForList(
            "SELECT amenity_id FROM guesthouse_amenity WHERE guesthouse_id = ? ORDER BY display_order DESC",
            UUID.class,
            nisztor);
    mockMvc
        .perform(
            put("/api/admin/guesthouses/{guesthouseId}/amenities/order", nisztor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"amenityIds\": ["
                        + ids.stream()
                            .map(id -> "\"" + id + "\"")
                            .collect(java.util.stream.Collectors.joining(","))
                        + "]}"))
        .andExpect(status().isNoContent());
  }

  private UUID guesthouseId(String slug) {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM guesthouse WHERE slug = ?", UUID.class, slug);
  }

  private UUID amenityId(String code) {
    return jdbcTemplate.queryForObject("SELECT id FROM amenity WHERE code = ?", UUID.class, code);
  }

  private int nextOrder(UUID guesthouseId) {
    Integer value =
        jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(display_order), -1) + 1 FROM guesthouse_amenity WHERE guesthouse_id = ?",
            Integer.class,
            guesthouseId);
    return value == null ? 0 : value;
  }

  private String request(
      String code,
      String category,
      String pricingType,
      UUID firstGuesthouse,
      Integer firstOrder,
      UUID secondGuesthouse,
      Integer secondOrder,
      boolean firstActive) {
    String secondAssignment =
        secondGuesthouse == null
            ? ""
            : ", {\"guesthouseId\": \"%s\", \"active\": true, \"displayOrder\": %d}"
                .formatted(secondGuesthouse, secondOrder);
    return """
        {
          "code": "%s",
          "category": "%s",
          "pricingType": "%s",
          "translations": [
            {"language": "hu", "name": "Asztalitenisz", "description": "Ingyenes játék", "detailedDescription": "Fedett helyen is játszható."},
            {"language": "ro", "name": "", "description": "", "detailedDescription": ""},
            {"language": "en", "name": "", "description": "", "detailedDescription": ""}
          ],
          "assignments": [
            {"guesthouseId": "%s", "active": %s, "displayOrder": %d}%s
          ]
        }
        """
        .formatted(
            code,
            category,
            pricingType,
            firstGuesthouse,
            firstActive,
            firstOrder,
            secondAssignment);
  }
}
