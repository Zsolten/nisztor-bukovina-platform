package com.bukovina.platform.tourism;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import com.bukovina.platform.tourism.startour.dto.StarTourTranslation;
import com.bukovina.platform.tourism.startour.dto.StarTourUpsertRequest;
import com.bukovina.platform.tourism.startour.exception.StarTourException;
import com.bukovina.platform.tourism.startour.service.StarTourService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import(PostgreSqlTestContainerConfiguration.class)
@Transactional
class StarTourStopControllerTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcClient jdbc;
  @Autowired private StarTourService starTourService;

  @Test
  @WithMockUser(roles = "ADMIN")
  void calculatesTotalsFromTheOrderedMatrixAndBlocksPublicationUntilEveryLegIsReady()
      throws Exception {
    UUID tourId = UUID.randomUUID();
    insertDraftTour(tourId);
    UUID paring = attractionId("paring-hegyseg");
    UUID veka = attractionId("veka-szurdok");
    UUID boli = attractionId("boli-barlang");
    deleteMatrixRows(paring, veka, boli);

    mockMvc
        .perform(
            put("/api/admin/tourism/star-tours/{id}/stops", tourId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(stopPlan(veka, boli, paring)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stops.length()").value(3))
        .andExpect(jsonPath("$.stops[0].slug").value("veka-szurdok"))
        .andExpect(jsonPath("$.stops[1].effectiveVisitDurationMinutes").value(45))
        .andExpect(jsonPath("$.totals.routeDataComplete").value(false));

    StarTourException rejected =
        assertThrows(
            StarTourException.class, () -> starTourService.update(tourId, publishedTour()));
    assertEquals("STAR_TOUR_ROUTE_DATA_INCOMPLETE", rejected.code());

    insertSuccessfulPair(veka, boli, 1_000, 120);
    insertSuccessfulPair(boli, paring, 2_000, 240);
    insertSuccessfulPair(paring, veka, 4_000, 480);

    mockMvc
        .perform(get("/api/admin/tourism/star-tours/{id}/stops", tourId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totals.routeDataComplete").value(true))
        .andExpect(jsonPath("$.totals.travelDistanceMeters").value(3_000))
        .andExpect(jsonPath("$.totals.travelDurationSeconds").value(360))
        .andExpect(jsonPath("$.totals.visitDurationMinutes").value(195))
        .andExpect(jsonPath("$.totals.totalDurationSeconds").value(12_060));

    mockMvc
        .perform(
            put("/api/admin/tourism/star-tours/{id}/stops", tourId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reorderedStopPlan(paring, veka, boli)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stops[0].slug").value("paring-hegyseg"))
        .andExpect(jsonPath("$.totals.travelDistanceMeters").value(5_000))
        .andExpect(jsonPath("$.totals.travelDurationSeconds").value(600));

    starTourService.update(tourId, publishedTour());
    mockMvc
        .perform(get("/api/tourism/star-tours/matrix-tesztut").param("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totals.travelDistanceMeters").value(5_000))
        .andExpect(jsonPath("$.totals.totalDurationSeconds").value(12_300));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void keepsOptionalStopOrdersStableAcrossRepeatedCoreStopSaves() throws Exception {
    UUID tourId =
        jdbc.sql("SELECT id FROM star_tour WHERE slug = 'paring-es-hatszegi-medence'")
            .query(UUID.class)
            .single();
    String plan =
        fourStopPlan(
            attractionId("paring-hegyseg"),
            attractionId("veka-szurdok"),
            attractionId("boli-barlang"),
            attractionId("vajdahunyadi-kastely"));

    saveStopPlan(tourId, plan);
    assertEquals(List.of(4, 5), optionalStopOrders(tourId));

    saveStopPlan(tourId, plan);
    assertEquals(List.of(4, 5), optionalStopOrders(tourId));
  }

  private void saveStopPlan(UUID tourId, String plan) throws Exception {
    mockMvc
        .perform(
            put("/api/admin/tourism/star-tours/{id}/stops", tourId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(plan))
        .andExpect(status().isOk());
  }

  private List<Integer> optionalStopOrders(UUID tourId) {
    return jdbc.sql(
            "SELECT display_order FROM star_tour_attraction WHERE star_tour_id = :tourId "
                + "AND optional_stop = TRUE ORDER BY display_order")
        .param("tourId", tourId)
        .query(Integer.class)
        .list();
  }

  private void insertDraftTour(UUID tourId) {
    jdbc.sql(
            "INSERT INTO star_tour (id, slug, map_color, published, active) "
                + "VALUES (:id, 'matrix-tesztut', '#336699', FALSE, TRUE)")
        .param("id", tourId)
        .update();
  }

  private UUID attractionId(String slug) {
    return jdbc.sql("SELECT id FROM attraction WHERE slug = :slug")
        .param("slug", slug)
        .query(UUID.class)
        .single();
  }

  private void deleteMatrixRows(UUID... attractionIds) {
    jdbc.sql(
            "DELETE FROM attraction_driving_distance WHERE attraction_a_id IN (:ids) OR attraction_b_id IN (:ids)")
        .param("ids", List.of(attractionIds))
        .update();
  }

  private void insertSuccessfulPair(UUID one, UUID two, int distanceMeters, int durationSeconds) {
    UUID first = one.toString().compareTo(two.toString()) < 0 ? one : two;
    UUID second = first.equals(one) ? two : one;
    jdbc.sql(
            "INSERT INTO attraction_driving_distance (attraction_a_id, attraction_b_id, distance_meters, "
                + "duration_seconds, calculation_status, source, calculated_at) "
                + "VALUES (:first, :second, :distance, :duration, 'SUCCESS', 'TEST', CURRENT_TIMESTAMP)")
        .param("first", first)
        .param("second", second)
        .param("distance", distanceMeters)
        .param("duration", durationSeconds)
        .update();
  }

  private StarTourUpsertRequest publishedTour() {
    return new StarTourUpsertRequest(
        "matrix-tesztut",
        "#336699",
        true,
        true,
        List.of(
            new StarTourTranslation("hu", "Mátrix tesztút", "Rövid leírás", "Részletes leírás.")),
        List.of(),
        List.of());
  }

  private String stopPlan(UUID first, UUID second, UUID third) {
    return """
        {
          "stops": [
            {"attractionId": "%s", "plannedVisitDurationMinutes": null},
            {"attractionId": "%s", "plannedVisitDurationMinutes": 45},
            {"attractionId": "%s", "plannedVisitDurationMinutes": null}
          ]
        }
        """
        .formatted(first, second, third);
  }

  private String reorderedStopPlan(UUID first, UUID second, UUID third) {
    return """
        {
          "stops": [
            {"attractionId": "%s", "plannedVisitDurationMinutes": null},
            {"attractionId": "%s", "plannedVisitDurationMinutes": null},
            {"attractionId": "%s", "plannedVisitDurationMinutes": 45}
          ]
        }
        """
        .formatted(first, second, third);
  }

  private String fourStopPlan(UUID first, UUID second, UUID third, UUID fourth) {
    return """
        {
          "stops": [
            {"attractionId": "%s", "plannedVisitDurationMinutes": null},
            {"attractionId": "%s", "plannedVisitDurationMinutes": null},
            {"attractionId": "%s", "plannedVisitDurationMinutes": null},
            {"attractionId": "%s", "plannedVisitDurationMinutes": null}
          ]
        }
        """
        .formatted(first, second, third, fourth);
  }
}
