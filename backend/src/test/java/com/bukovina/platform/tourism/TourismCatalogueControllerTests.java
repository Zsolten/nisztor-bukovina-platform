package com.bukovina.platform.tourism;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import(PostgreSqlTestContainerConfiguration.class)
@Transactional
class TourismCatalogueControllerTests {
  @Autowired private MockMvc mockMvc;

  @Test
  void protectsTourismAdministrationAndOnlyPublishesAvailableTranslations() throws Exception {
    mockMvc.perform(get("/api/admin/tourism/attractions")).andExpect(status().isUnauthorized());
    mockMvc
        .perform(get("/api/tourism/attractions").param("lang", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
    mockMvc
        .perform(get("/api/tourism/attractions/paring-hegyseg").param("lang", "en"))
        .andExpect(status().isNotFound());
    mockMvc
        .perform(get("/api/tourism/collections").param("lang", "ro"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
    mockMvc
        .perform(get("/api/tourism/attractions").param("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(10))
        .andExpect(jsonPath("$[?(@.slug == 'paring-hegyseg')].name").value("Páring-hegység"))
        .andExpect(jsonPath("$[0].active").doesNotExist());
    mockMvc
        .perform(get("/api/tourism/star-tours").param("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
    mockMvc
        .perform(get("/api/tourism/star-tours/paring-es-hatszegi-medence").param("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stops.length()").value(6))
        .andExpect(jsonPath("$.stops[3].optional").value(false))
        .andExpect(jsonPath("$.stops[4].optional").value(true))
        .andExpect(jsonPath("$.stops[5].optional").value(true));
    mockMvc
        .perform(get("/api/tourism/star-tours").param("lang", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void validatesCoordinatesAndKeepsDraftToursOutOfThePublicCatalogue() throws Exception {
    mockMvc
        .perform(
            post("/api/admin/tourism/attractions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(attractionRequest(91)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_LATITUDE"));
    mockMvc
        .perform(
            post("/api/admin/tourism/attractions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(attractionRequest(45).replace("{", "{\"id\":\"ignored\",")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_ADMIN_ATTRACTION_REQUEST"));
    mockMvc
        .perform(
            post("/api/admin/tourism/attractions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(attractionRequest(45).replace("\"active\": true,", "")))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(
            post("/api/admin/tourism/star-tours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tourRequest(false).replace("\"published\": false,", "")))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(
            post("/api/admin/tourism/star-tours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tourRequest(false).replace("\"active\": true,", "")))
        .andExpect(status().isBadRequest());

    String location =
        mockMvc
            .perform(
                post("/api/admin/tourism/star-tours")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(tourRequest(false)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.slug").value("teszt-korut"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String id =
        new com.fasterxml.jackson.databind.ObjectMapper().readTree(location).get("id").asText();

    mockMvc.perform(get("/api/tourism/star-tours/teszt-korut")).andExpect(status().isNotFound());
    mockMvc
        .perform(
            put("/api/admin/tourism/star-tours/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(tourRequest(true)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.published").value(true));
    mockMvc
        .perform(get("/api/tourism/star-tours/teszt-korut").param("lang", "ro"))
        .andExpect(status().isNotFound());
    mockMvc
        .perform(get("/api/tourism/star-tours/teszt-korut").param("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Teszt körút"))
        .andExpect(jsonPath("$.published").doesNotExist())
        .andExpect(jsonPath("$.active").doesNotExist());
  }

  private String attractionRequest(int latitude) {
    return """
        {
          "slug": "hibas-koordinata",
          "latitude": %d,
          "longitude": 23.1,
          "googleMapsUrl": "https://maps.google.com/?q=91,23.1",
          "active": true,
          "collectionSlugs": [],
          "translations": [{
            "language": "hu",
            "name": "Hibás koordináta",
            "shortDescription": "Rövid leírás",
            "detailedDescription": "Hosszú leírás",
            "admissionInformation": "",
            "practicalInformation": ""
          }]
        }
        """
        .formatted(latitude);
  }

  private String tourRequest(boolean published) {
    return """
        {
          "slug": "teszt-korut",
          "mapColor": "#336699",
          "published": %s,
          "active": true,
          "tags": ["családi"],
          "images": [{"imageUrl": "https://example.com/tour.jpg", "altText": "Tájkép"}],
          "translations": [{
            "language": "hu",
            "name": "Teszt körút",
            "shortDescription": "Egy rövid próbaút.",
            "detailedDescription": "A csillagtúra részletes magyar bemutatása."
          }]
        }
        """
        .formatted(published);
  }
}
