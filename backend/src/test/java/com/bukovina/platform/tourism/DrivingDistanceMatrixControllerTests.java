package com.bukovina.platform.tourism;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import com.bukovina.platform.tourism.routing.DrivingDistanceProvider;
import com.bukovina.platform.tourism.routing.DrivingDistanceProvider.AttractionPoint;
import com.bukovina.platform.tourism.routing.DrivingDistanceProvider.MatrixElement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
  DrivingDistanceMatrixControllerTests.TestRouting.class
})
@Transactional
class DrivingDistanceMatrixControllerTests {
  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcClient jdbc;
  @Autowired private TestDrivingDistanceProvider provider;

  @Test
  @WithMockUser(roles = "ADMIN")
  void createsOneRowPerAffectedPairAndRecordsFailures() throws Exception {
    String id =
        new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(
                mockMvc
                    .perform(
                        post("/api/admin/tourism/attractions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(attractionRequest(45.11)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.distanceCalculation.total").value(10))
                    .andExpect(jsonPath("$.distanceCalculation.successful").value(10))
                    .andExpect(jsonPath("$.distanceCalculation.failed").value(0))
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("id")
            .asText();
    assertEquals(1, provider.calls.get());
    assertEquals(
        10,
        jdbc.sql(
                "SELECT COUNT(*) FROM attraction_driving_distance "
                    + "WHERE attraction_a_id = :id OR attraction_b_id = :id")
            .param("id", java.util.UUID.fromString(id))
            .query(Integer.class)
            .single());

    mockMvc
        .perform(
            put("/api/admin/tourism/attractions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(attractionRequest(45.11)))
        .andExpect(status().isOk());
    assertEquals(1, provider.calls.get());

    mockMvc
        .perform(
            put("/api/admin/tourism/attractions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(attractionRequest(45.12)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.distanceCalculation.successful").value(10));
    assertEquals(2, provider.calls.get());

    provider.fail = true;
    mockMvc
        .perform(
            put("/api/admin/tourism/attractions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(attractionRequest(45.13)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.distanceCalculation.successful").value(0))
        .andExpect(jsonPath("$.distanceCalculation.failed").value(10));
    assertEquals(
        10,
        jdbc.sql(
                "SELECT COUNT(*) FROM attraction_driving_distance "
                    + "WHERE (attraction_a_id = :id OR attraction_b_id = :id) "
                    + "AND calculation_status = 'FAILED'")
            .param("id", java.util.UUID.fromString(id))
            .query(Integer.class)
            .single());
  }

  private static String attractionRequest(double latitude) {
    return """
        {
          "slug": "matrix-tesztpont",
          "latitude": %s,
          "longitude": 23.1,
          "googleMapsUrl": "https://maps.google.com/?q=45,23.1",
          "active": true,
          "collectionSlugs": [],
          "translations": [{
            "language": "hu",
            "name": "Mátrix tesztpont",
            "shortDescription": "Rövid leírás",
            "detailedDescription": "Hosszú leírás",
            "admissionInformation": "",
            "practicalInformation": ""
          }]
        }
        """
        .formatted(latitude);
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class TestRouting {
    @Bean
    @Primary
    TestDrivingDistanceProvider testDrivingDistanceProvider() {
      return new TestDrivingDistanceProvider();
    }
  }

  static class TestDrivingDistanceProvider implements DrivingDistanceProvider {
    private final AtomicInteger calls = new AtomicInteger();
    private boolean fail;

    @Override
    public List<MatrixElement> calculate(
        List<AttractionPoint> origins, List<AttractionPoint> destinations) {
      calls.incrementAndGet();
      if (fail) {
        throw new IllegalStateException("GOOGLE_ROUTES_REQUEST_FAILED");
      }
      List<MatrixElement> result = new ArrayList<>();
      for (int origin = 0; origin < origins.size(); origin++) {
        for (int destination = 0; destination < destinations.size(); destination++) {
          result.add(new MatrixElement(origin, destination, 12_345, 678, null));
        }
      }
      return result;
    }
  }
}
