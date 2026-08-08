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
        .andExpect(jsonPath("$[0].id").isNotEmpty())
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
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.slug").value("nisztor-panzio"))
        .andExpect(jsonPath("$.images.length()").value(10))
        .andExpect(jsonPath("$.coverImage.cover").value(true));

    mockMvc
        .perform(get("/api/guesthouses/bukovina-panzio").queryParam("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.slug").value("bukovina-panzio"))
        .andExpect(jsonPath("$.images.length()").value(16))
        .andExpect(jsonPath("$.coverImage.cover").value(true));
  }

  @Test
  void returnsLocalizedCompleteGuesthouseDetails() throws Exception {
    mockMvc
        .perform(get("/api/guesthouses/nisztor-panzio").queryParam("lang", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Nisztor Guesthouse"))
        .andExpect(jsonPath("$.history.title").value("Bukovina Szekler heritage in Cristur"))
        .andExpect(jsonPath("$.roomTypes.length()").value(3))
        .andExpect(jsonPath("$.roomTypes[0].quantity").value(3))
        .andExpect(jsonPath("$.amenities.length()").value(23))
        .andExpect(jsonPath("$.pricing.currency").value("RON"))
        .andExpect(jsonPath("$.pricing.items[0].amount").value(130))
        .andExpect(jsonPath("$.pricing.taxes[0].id").value("accommodation_tax"))
        .andExpect(jsonPath("$.pricing.taxes[0].percentage").value(11))
        .andExpect(jsonPath("$.pricing.taxes[1].id").value("city_tax"))
        .andExpect(jsonPath("$.pricing.taxes[1].percentage").value(1))
        .andExpect(jsonPath("$.pricing.items.length()").value(6))
        .andExpect(jsonPath("$.pricing.items[?(@.id == 'tour_guide')]").isEmpty())
        .andExpect(
            jsonPath("$.address.formatted")
                .value("17 Bucovina Street, Cristur 330003, Hunedoara County, Romania"))
        .andExpect(
            jsonPath("$.images[0].altText").value("Street-facing facade of Nisztor Guesthouse"));
  }

  @Test
  void exposesTourGuideOnlyInHungarian() throws Exception {
    mockMvc
        .perform(get("/api/guesthouses/nisztor-panzio").queryParam("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pricing.items[?(@.id == 'tour_guide')]").isNotEmpty());

    mockMvc
        .perform(get("/api/guesthouses/nisztor-panzio").queryParam("lang", "ro"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pricing.items[?(@.id == 'tour_guide')]").isEmpty());
  }

  @Test
  @Transactional
  void hidesInactiveRoomTypesAmenitiesAndPrices() throws Exception {
    jdbcTemplate.update(
        """
        UPDATE guesthouse_amenity assignment
        SET active = FALSE
        FROM guesthouse, amenity
        WHERE assignment.guesthouse_id = guesthouse.id
          AND assignment.amenity_id = amenity.id
          AND guesthouse.slug = 'nisztor-panzio'
          AND amenity.code = 'wifi'
        """);
    jdbcTemplate.update(
        """
        UPDATE room_type room_type
        SET active = FALSE
        FROM guesthouse
        WHERE room_type.guesthouse_id = guesthouse.id
          AND guesthouse.slug = 'nisztor-panzio'
          AND room_type.code = 'triple'
        """);
    jdbcTemplate.update(
        """
        UPDATE price_item item
        SET active = FALSE
        FROM guesthouse_pricing pricing, guesthouse
        WHERE item.pricing_id = pricing.id
          AND pricing.guesthouse_id = guesthouse.id
          AND guesthouse.slug = 'nisztor-panzio'
          AND item.code = 'breakfast'
        """);

    mockMvc
        .perform(get("/api/guesthouses/nisztor-panzio").queryParam("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roomTypes.length()").value(2))
        .andExpect(jsonPath("$.amenities.length()").value(22))
        .andExpect(jsonPath("$.pricing.items.length()").value(6));
  }

  @Test
  @Transactional
  void fallsBackToHungarianWhenTheRequestedTranslationIsMissing() throws Exception {
    jdbcTemplate.update(
        "DELETE FROM guesthouse_translation WHERE guesthouse_id = "
            + "(SELECT id FROM guesthouse WHERE slug = 'bukovina-panzio') "
            + "AND language_code = 'en'");
    jdbcTemplate.update(
        """
        DELETE FROM guesthouse_image_translation translation
        USING guesthouse_image image, guesthouse
        WHERE translation.image_id = image.id
          AND image.guesthouse_id = guesthouse.id
          AND guesthouse.slug = 'bukovina-panzio'
          AND translation.language_code = 'en'
        """);

    mockMvc
        .perform(get("/api/guesthouses/bukovina-panzio").queryParam("lang", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Bukovina Panzió"))
        .andExpect(
            jsonPath("$.coverImage.altText")
                .value("Kétágyas szoba napraforgós festménnyel a Bukovina Panzióban"));
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
