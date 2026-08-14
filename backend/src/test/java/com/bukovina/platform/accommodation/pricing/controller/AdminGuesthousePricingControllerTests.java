package com.bukovina.platform.accommodation.pricing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
class AdminGuesthousePricingControllerTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void protectsGuesthousePricesFromAnonymousUsers() throws Exception {
    mockMvc
        .perform(get("/api/admin/guesthouses/{guesthouseId}/pricing", guesthouseId()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updatesCurrentPricesAndMakesThemAvailableToThePublicApi() throws Exception {
    UUID guesthouseId = guesthouseId();
    mockMvc
        .perform(get("/api/admin/guesthouses/{guesthouseId}/pricing", guesthouseId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currency").value("RON"))
        .andExpect(jsonPath("$.items[?(@.code == 'accommodation')].amount").isNotEmpty());

    mockMvc
        .perform(
            put("/api/admin/guesthouses/{guesthouseId}/pricing", guesthouseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest(guesthouseId, new BigDecimal("199.00"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[?(@.code == 'accommodation')].amount[0]").value(199));

    mockMvc
        .perform(get("/api/guesthouses/nisztor-panzio").queryParam("lang", "hu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pricing.items[?(@.id == 'accommodation')].amount[0]").value(199));
  }

  private UUID guesthouseId() {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM guesthouse WHERE slug = 'nisztor-panzio'", UUID.class);
  }

  private String updateRequest(UUID guesthouseId, BigDecimal accommodationAmount) {
    UUID pricingId =
        jdbcTemplate.queryForObject(
            "SELECT id FROM guesthouse_pricing WHERE guesthouse_id = ? AND active = TRUE",
            UUID.class,
            guesthouseId);
    List<Map<String, Object>> items =
        jdbcTemplate.queryForList(
            "SELECT code, amount FROM price_item WHERE pricing_id = ? AND active = TRUE ORDER BY display_order",
            pricingId);
    List<Map<String, Object>> surcharges = adjustments(pricingId, "SURCHARGE");
    List<Map<String, Object>> discounts = adjustments(pricingId, "DISCOUNT");
    return """
        {"items": [%s], "surcharges": [%s], "discounts": [%s]}
        """
        .formatted(
            items.stream()
                .map(
                    item ->
                        "{\"code\":\"%s\",\"amount\":%s}"
                            .formatted(
                                item.get("code"),
                                "accommodation".equals(item.get("code"))
                                    ? accommodationAmount
                                    : item.get("amount")))
                .collect(Collectors.joining(",")),
            adjustmentsJson(surcharges),
            adjustmentsJson(discounts));
  }

  private List<Map<String, Object>> adjustments(UUID pricingId, String kind) {
    return jdbcTemplate.queryForList(
        "SELECT code, percentage FROM pricing_adjustment WHERE pricing_id = ? AND kind = ? AND active = TRUE ORDER BY display_order",
        pricingId,
        kind);
  }

  private String adjustmentsJson(List<Map<String, Object>> adjustments) {
    return adjustments.stream()
        .map(
            adjustment ->
                "{\"code\":\"%s\",\"percentage\":%s}"
                    .formatted(adjustment.get("code"), adjustment.get("percentage")))
        .collect(Collectors.joining(","));
  }
}
