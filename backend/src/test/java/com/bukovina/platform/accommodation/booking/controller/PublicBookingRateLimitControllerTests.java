package com.bukovina.platform.accommodation.booking.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
      "DB_PASSWORD=test-password",
      "booking.public.rate-limit.enabled=true",
      "booking.public.rate-limit.quote-client-capacity=1",
      "booking.public.rate-limit.request-client-capacity=1",
      "booking.public.rate-limit.request-email-capacity=1"
    })
@AutoConfigureMockMvc
@Import(PostgreSqlTestContainerConfiguration.class)
@Transactional
class PublicBookingRateLimitControllerTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void limitsRepeatedQuoteRequestsFromTheSameClient() throws Exception {
    String request = quoteJson();

    mockMvc
        .perform(
            post("/api/booking-quotes").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/booking-quotes").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"))
        .andExpect(jsonPath("$.code").value("BOOKING_RATE_LIMITED"))
        .andExpect(jsonPath("$.errors").isEmpty())
        .andExpect(jsonPath("$.currentQuote").doesNotExist());
  }

  private String quoteJson() {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    return """
        {
          "guesthouseId": "%s",
          "checkInDate": "%s",
          "checkOutDate": "%s",
          "adults": 2,
          "childrenAge3to10": 0,
          "childrenAge0to3": 0,
          "roomSelections": [{"roomTypeId": "%s", "quantity": 1}],
          "services": {"breakfastParticipants": 0, "dinnerParticipants": 0}
        }
        """
        .formatted(
            guesthouseId, LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), roomTypeId);
  }

  private UUID guesthouseId(String slug) {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM guesthouse WHERE slug = ?", UUID.class, slug);
  }

  private UUID roomTypeId(String guesthouseSlug, String code) {
    return jdbcTemplate.queryForObject(
        """
        SELECT room.id
        FROM room_type room
        JOIN guesthouse ON guesthouse.id = room.guesthouse_id
        WHERE guesthouse.slug = ? AND room.code = ?
        """,
        UUID.class,
        guesthouseSlug,
        code);
  }
}
