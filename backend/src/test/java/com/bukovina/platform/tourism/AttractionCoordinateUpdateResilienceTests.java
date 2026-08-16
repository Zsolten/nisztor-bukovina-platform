package com.bukovina.platform.tourism;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import com.bukovina.platform.tourism.routing.DrivingDistanceMatrixService;
import com.bukovina.platform.tourism.routing.DrivingDistanceProvider;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import({
  PostgreSqlTestContainerConfiguration.class,
  AttractionCoordinateUpdateResilienceTests.FailingDistanceCalculation.class
})
@Transactional
class AttractionCoordinateUpdateResilienceTests {
  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcClient jdbc;

  @Test
  @WithMockUser(roles = "ADMIN")
  void savesChangedCoordinatesWhenDistanceRecalculationFailsUnexpectedly() throws Exception {
    UUID id =
        jdbc.sql("SELECT id FROM attraction WHERE slug = 'paring-hegyseg'")
            .query(UUID.class)
            .single();

    mockMvc
        .perform(
            put("/api/admin/tourism/attractions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request("paring-hegyseg", 45.6926924, 23.0696974, "Páring-hegység")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.latitude").value(45.692692))
        .andExpect(jsonPath("$.longitude").value(23.069697));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void preservesUniqueOrderingWhenMultipleAttractionsInTheSameCollectionAreUpdated()
      throws Exception {
    updateExistingAttraction("paring-hegyseg", "Páring-hegység");
    updateExistingAttraction("veka-szurdok", "Véka-szurdok");
  }

  private void updateExistingAttraction(String slug, String name) throws Exception {
    AttractionCoordinates attraction =
        jdbc.sql("SELECT id, latitude, longitude FROM attraction WHERE slug = :slug")
            .param("slug", slug)
            .query(
                (rs, row) ->
                    new AttractionCoordinates(
                        rs.getObject("id", UUID.class),
                        rs.getBigDecimal("latitude"),
                        rs.getBigDecimal("longitude")))
            .single();

    mockMvc
        .perform(
            put("/api/admin/tourism/attractions/{id}", attraction.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    request(
                        slug,
                        attraction.latitude().doubleValue(),
                        attraction.longitude().doubleValue(),
                        name)))
        .andExpect(status().isOk());
  }

  private static String request(String slug, double latitude, double longitude, String name) {
    return """
        {
          "slug": "%s",
          "latitude": %s,
          "longitude": %s,
          "googleMapsUrl": "https://www.google.com/maps/place/Telescaun+Parang",
          "recommendedVisitDurationMinutes": 120,
          "active": true,
          "collectionSlugs": ["hunyadiak-hagyateka-hatszegi-medence"],
          "translations": [{
            "language": "hu",
            "name": "%s",
            "shortDescription": "Rövid leírás",
            "detailedDescription": "Hosszú leírás",
            "admissionInformation": "42 lej/fő",
            "practicalInformation": null
          }]
        }
        """
        .formatted(slug, latitude, longitude, name);
  }

  private record AttractionCoordinates(UUID id, BigDecimal latitude, BigDecimal longitude) {}

  @TestConfiguration(proxyBeanMethods = false)
  static class FailingDistanceCalculation {
    @Bean
    @Primary
    DrivingDistanceMatrixService failingDrivingDistanceMatrixService(
        JdbcClient jdbc, DrivingDistanceProvider provider) {
      return new DrivingDistanceMatrixService(jdbc, provider) {
        @Override
        public CalculationSummary recalculateAffectedPairs(UUID attractionId) {
          throw new IllegalStateException("DISTANCE_MATRIX_STORAGE_FAILED");
        }
      };
    }
  }
}
