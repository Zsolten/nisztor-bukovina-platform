package com.bukovina.platform.accommodation.guesthouse.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import(PostgreSqlTestContainerConfiguration.class)
class PublicGuesthouseControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void listsBothActiveGuesthousesInDisplayOrder() throws Exception {
    mockMvc
        .perform(get("/api/guesthouses").queryParam("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].slug").value("nisztor-panzio"))
        .andExpect(jsonPath("$[0].name").value("Nisztor Panzió"))
        .andExpect(jsonPath("$[0].roomCount").value(5))
        .andExpect(jsonPath("$[1].slug").value("bukovina-panzio"))
        .andExpect(jsonPath("$[1].name").value("Bukovina Panzió"))
        .andExpect(jsonPath("$[1].roomCount").value(12));
  }

  @Test
  void returnsTheRequestedGuesthouseWithItsGallery() throws Exception {
    mockMvc
        .perform(get("/api/guesthouses/nisztor-panzio").queryParam("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.slug").value("nisztor-panzio"))
        .andExpect(jsonPath("$.images.length()").value(26))
        .andExpect(jsonPath("$.coverImage.cover").value(true));

    mockMvc
        .perform(get("/api/guesthouses/bukovina-panzio").queryParam("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.slug").value("bukovina-panzio"))
        .andExpect(jsonPath("$.images.length()").value(34))
        .andExpect(jsonPath("$.coverImage.cover").value(true));
  }

  @Test
  void fallsBackToHungarianWhenTheRequestedTranslationIsMissing() throws Exception {
    mockMvc
        .perform(get("/api/guesthouses/bukovina-panzio").queryParam("lang", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Bukovina Panzió"));
  }

  @Test
  @Transactional
  void hidesAnInactiveGuesthouseFromListAndDetail() throws Exception {
    jdbcTemplate.update("UPDATE guesthouse SET active = FALSE WHERE slug = ?", "nisztor-panzio");

    mockMvc
        .perform(get("/api/guesthouses"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].slug").value("bukovina-panzio"));
    mockMvc.perform(get("/api/guesthouses/nisztor-panzio")).andExpect(status().isNotFound());
  }

  @Test
  void rejectsUnsupportedLanguages() throws Exception {
    mockMvc
        .perform(get("/api/guesthouses").queryParam("lang", "de"))
        .andExpect(status().isBadRequest());
  }
}
