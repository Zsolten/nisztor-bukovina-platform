package com.bukovina.platform.accommodation.guesthouse.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
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
class AdminGuesthouseContentControllerTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void protectsContentEndpointsFromAnonymousUsers() throws Exception {
    mockMvc.perform(get("/api/admin/guesthouses/content")).andExpect(status().isUnauthorized());
    mockMvc
        .perform(
            put("/api/admin/guesthouses/{guesthouseId}/translations/hu", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("null", "Név")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USER")
  void rejectsUsersWithoutTheAdministratorRole() throws Exception {
    mockMvc.perform(get("/api/admin/guesthouses/content")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void listsActiveAndInactiveGuesthousesWithRawTranslations() throws Exception {
    jdbcTemplate.update("UPDATE guesthouse SET active = FALSE WHERE slug = 'bukovina-panzio'");

    mockMvc
        .perform(get("/api/admin/guesthouses/content"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].slug").value("nisztor-panzio"))
        .andExpect(jsonPath("$[0].translations.length()").value(3))
        .andExpect(jsonPath("$[0].translations[0].language").value("hu"))
        .andExpect(jsonPath("$[0].translations[1].language").value("ro"))
        .andExpect(jsonPath("$[0].translations[2].language").value("en"))
        .andExpect(jsonPath("$[1].slug").value("bukovina-panzio"))
        .andExpect(jsonPath("$[1].active").value(false));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updatesOneLanguageAndMakesItImmediatelyPublic() throws Exception {
    UUID guesthouseId = guesthouseId("nisztor-panzio");

    mockMvc
        .perform(
            put("/api/admin/guesthouses/{guesthouseId}/translations/en", guesthouseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("0", "Updated English name")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.language").value("en"))
        .andExpect(jsonPath("$.version").value(1))
        .andExpect(jsonPath("$.name").value("Updated English name"));

    mockMvc
        .perform(get("/api/guesthouses/nisztor-panzio").param("lang", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated English name"));
    mockMvc
        .perform(get("/api/guesthouses/nisztor-panzio").param("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Nisztor Panzió"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createsAMissingTranslationWithoutCopyingTheHungarianFallback() throws Exception {
    UUID guesthouseId = guesthouseId("nisztor-panzio");
    jdbcTemplate.update(
        "DELETE FROM guesthouse_translation WHERE guesthouse_id = ? AND language_code = 'ro'",
        guesthouseId);

    mockMvc
        .perform(get("/api/admin/guesthouses/content"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].translations[1].version").value(nullValue()))
        .andExpect(jsonPath("$[0].translations[?(@.language == 'ro')].name").value(""));

    mockMvc
        .perform(
            put("/api/admin/guesthouses/{guesthouseId}/translations/ro", guesthouseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("null", "Nume nou")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value(0))
        .andExpect(jsonPath("$.name").value("Nume nou"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void rejectsBlankAndOverlongFieldsWithStableFieldCodes() throws Exception {
    UUID guesthouseId = guesthouseId("nisztor-panzio");
    String overlongTitle = "x".repeat(241);

    mockMvc
        .perform(
            put("/api/admin/guesthouses/{guesthouseId}/translations/hu", guesthouseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest(overlongTitle)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ADMIN_CONTENT_VALIDATION_FAILED"))
        .andExpect(jsonPath("$.fieldErrors.name").value("REQUIRED"))
        .andExpect(jsonPath("$.fieldErrors.historyTitle").value("TOO_LONG"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void rejectsUnsupportedLanguagesAndUnknownGuesthouses() throws Exception {
    mockMvc
        .perform(
            put(
                    "/api/admin/guesthouses/{guesthouseId}/translations/de",
                    guesthouseId("nisztor-panzio"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("0", "Name")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("UNSUPPORTED_CONTENT_LANGUAGE"));

    mockMvc
        .perform(
            put("/api/admin/guesthouses/{guesthouseId}/translations/hu", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("0", "Name")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ADMIN_GUESTHOUSE_NOT_FOUND"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void rejectsAnOutdatedVersionAndReturnsTheCurrentContent() throws Exception {
    UUID guesthouseId = guesthouseId("nisztor-panzio");

    mockMvc
        .perform(
            put("/api/admin/guesthouses/{guesthouseId}/translations/hu", guesthouseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("0", "First update")))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            put("/api/admin/guesthouses/{guesthouseId}/translations/hu", guesthouseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("0", "Stale update")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ADMIN_CONTENT_VERSION_CONFLICT"))
        .andExpect(jsonPath("$.currentContent.version").value(1))
        .andExpect(jsonPath("$.currentContent.name").value("First update"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void preservesMarkupAsPlainContentData() throws Exception {
    UUID guesthouseId = guesthouseId("nisztor-panzio");

    mockMvc
        .perform(
            put("/api/admin/guesthouses/{guesthouseId}/translations/hu", guesthouseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("0", "<script>alert('x')</script>")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("<script>alert('x')</script>"));
  }

  private UUID guesthouseId(String slug) {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM guesthouse WHERE slug = ?", UUID.class, slug);
  }

  private String validRequest(String version, String name) {
    return """
        {
          "version": %s,
          "name": "%s",
          "shortDescription": "Rövid leírás",
          "description": "Részletes leírás",
          "roomDescription": "Szobák bevezetője",
          "historyTitle": "Történet címe",
          "historyText": "Történet szövege"
        }
        """
        .formatted(version, name);
  }

  private String invalidRequest(String historyTitle) {
    return """
        {
          "version": 0,
          "name": "   ",
          "shortDescription": "Rövid leírás",
          "description": "Részletes leírás",
          "roomDescription": "Szobák bevezetője",
          "historyTitle": "%s",
          "historyText": "Történet szövege"
        }
        """
        .formatted(historyTitle);
  }
}
