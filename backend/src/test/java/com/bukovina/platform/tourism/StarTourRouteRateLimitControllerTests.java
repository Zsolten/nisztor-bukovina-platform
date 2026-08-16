package com.bukovina.platform.tourism;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RouteLeg;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RoutePoint;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
      "DB_PASSWORD=test-password",
      "tourism.routing.star-tour.rate-limit.enabled=true",
      "tourism.routing.star-tour.rate-limit.global-capacity=1",
      "tourism.routing.star-tour.rate-limit.client-capacity=1"
    })
@AutoConfigureMockMvc
@Import({
  PostgreSqlTestContainerConfiguration.class,
  StarTourRouteRateLimitControllerTests.RouteProviderConfiguration.class
})
@Transactional
class StarTourRouteRateLimitControllerTests {

  @Autowired private MockMvc mockMvc;

  @Test
  void doesNotChargeCachedRoutesButRejectsTheNextColdCombination() throws Exception {
    mockMvc
        .perform(get("/api/tourism/star-tours/paring-es-hatszegi-medence/route"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.routeStatus").value("READY"))
        .andExpect(jsonPath("$.cached").value(false));
    mockMvc
        .perform(get("/api/tourism/star-tours/paring-es-hatszegi-medence/route"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cached").value(true));
    mockMvc
        .perform(
            get("/api/tourism/star-tours/paring-es-hatszegi-medence/route")
                .param("optionalStopSlug", "demsusi-kotemplom"))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.code", startsWith("STAR_TOUR_ROUTE_RATE_LIMITED_")));
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class RouteProviderConfiguration {
    @Bean
    @Primary
    DrivingRouteProvider testDrivingRouteProvider() {
      return new DrivingRouteProvider() {
        @Override
        public List<RouteLeg> calculate(
            RoutePoint origin, List<RoutePoint> intermediates, RoutePoint destination) {
          return java.util.stream.IntStream.rangeClosed(0, intermediates.size())
              .mapToObj(index -> new RouteLeg(1_000 + index, 100 + index, "polyline-" + index))
              .toList();
        }
      };
    }
  }
}
