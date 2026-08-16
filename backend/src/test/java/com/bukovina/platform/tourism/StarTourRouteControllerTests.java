package com.bukovina.platform.tourism;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RouteLeg;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RoutePoint;
import com.bukovina.platform.tourism.routing.StarTourRouteCacheInvalidator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import({
  PostgreSqlTestContainerConfiguration.class,
  StarTourRouteControllerTests.RouteProviderConfiguration.class
})
@Transactional
class StarTourRouteControllerTests {
  private static final String TOUR_SLUG = "paring-es-hatszegi-medence";
  private static final String PARING_ID = "5a09d762-ae0a-520e-a0da-b99d9a958a14";

  @Autowired private MockMvc mockMvc;
  @Autowired private TestDrivingRouteProvider provider;
  @Autowired private StarTourRouteCacheInvalidator cacheInvalidator;
  @Autowired private JdbcClient jdbc;

  @Test
  void calculatesEachOptionalStopCombinationOnceAndInvalidatesAffectedRoutes() throws Exception {
    mockMvc
        .perform(get("/api/tourism/star-tours/{slug}/route", TOUR_SLUG))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cached").value(false))
        .andExpect(jsonPath("$.base.latitude").value(45.8232811))
        .andExpect(jsonPath("$.stops.length()").value(4))
        .andExpect(jsonPath("$.legs.length()").value(5))
        .andExpect(jsonPath("$.legs[0].fromStopIndex").value(0))
        .andExpect(jsonPath("$.legs[4].toStopIndex").value(5));

    mockMvc
        .perform(get("/api/tourism/star-tours/{slug}/route", TOUR_SLUG))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cached").value(true));
    org.junit.jupiter.api.Assertions.assertEquals(1, provider.calls.get());

    mockMvc
        .perform(
            get("/api/tourism/star-tours/{slug}/route", TOUR_SLUG)
                .param("optionalStopSlug", "demsusi-kotemplom"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cached").value(false))
        .andExpect(jsonPath("$.stops.length()").value(5))
        .andExpect(jsonPath("$.legs.length()").value(6));
    org.junit.jupiter.api.Assertions.assertEquals(2, provider.calls.get());

    cacheInvalidator.invalidateForAttraction(java.util.UUID.fromString(PARING_ID));
    mockMvc
        .perform(get("/api/tourism/star-tours/{slug}/route", TOUR_SLUG))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cached").value(false));
    org.junit.jupiter.api.Assertions.assertEquals(3, provider.calls.get());
  }

  @Test
  void rejectsAnOptionalStopThatDoesNotBelongToTheTour() throws Exception {
    mockMvc
        .perform(
            get("/api/tourism/star-tours/{slug}/route", TOUR_SLUG)
                .param("optionalStopSlug", "deva-vara"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_OPTIONAL_STOPS"));
  }

  @Test
  void storesAFailedCalculationAndDoesNotCallGoogleForMoreThanTenStops() throws Exception {
    provider.fail.set(true);
    mockMvc
        .perform(get("/api/tourism/star-tours/{slug}/route", "maros-mente-es-gyulafehervar"))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.code").value("STAR_TOUR_ROUTE_CALCULATION_FAILED"));
    org.junit.jupiter.api.Assertions.assertEquals(
        "FAILED",
        jdbc.sql(
                "SELECT calculation_status FROM star_tour_route_variant variant "
                    + "JOIN star_tour tour ON tour.id = variant.star_tour_id "
                    + "WHERE tour.slug = :slug")
            .param("slug", "maros-mente-es-gyulafehervar")
            .query(String.class)
            .single());
    provider.fail.set(false);

    for (int index = 0; index < 7; index++) {
      UUID attractionId = UUID.randomUUID();
      jdbc.sql(
              "INSERT INTO attraction (id, slug, latitude, longitude, google_maps_url, active) "
                  + "VALUES (:id, :slug, 45.1, 23.1, 'https://example.com', TRUE)")
          .param("id", attractionId)
          .param("slug", "route-limit-" + index)
          .update();
      jdbc.sql(
              "INSERT INTO star_tour_attraction (star_tour_id, attraction_id, display_order, optional_stop) "
                  + "VALUES ('1cb58299-6d38-5e68-a571-594b1c6bd5dd', :attractionId, :displayOrder, FALSE)")
          .param("attractionId", attractionId)
          .param("displayOrder", 100 + index)
          .update();
    }

    int callsBeforeLimitCheck = provider.calls.get();
    mockMvc
        .perform(get("/api/tourism/star-tours/{slug}/route", TOUR_SLUG))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("STAR_TOUR_STOP_LIMIT_EXCEEDED"));
    org.junit.jupiter.api.Assertions.assertEquals(callsBeforeLimitCheck, provider.calls.get());
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class RouteProviderConfiguration {
    @Bean
    @Primary
    TestDrivingRouteProvider testDrivingRouteProvider() {
      return new TestDrivingRouteProvider();
    }
  }

  static class TestDrivingRouteProvider implements DrivingRouteProvider {
    private final AtomicInteger calls = new AtomicInteger();
    private final AtomicBoolean fail = new AtomicBoolean();

    @Override
    public List<RouteLeg> calculate(
        RoutePoint origin, List<RoutePoint> intermediates, RoutePoint destination) {
      calls.incrementAndGet();
      if (fail.get()) {
        throw new IllegalStateException("synthetic route failure");
      }
      return java.util.stream.IntStream.rangeClosed(0, intermediates.size())
          .mapToObj(index -> new RouteLeg(1_000 + index, 100 + index, "polyline-" + index))
          .toList();
    }
  }
}
