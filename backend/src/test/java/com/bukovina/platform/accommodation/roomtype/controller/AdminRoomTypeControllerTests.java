package com.bukovina.platform.accommodation.roomtype.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import java.util.UUID;
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
class AdminRoomTypeControllerTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void protectsRoomTypesFromAnonymousUsers() throws Exception {
    mockMvc
        .perform(get("/api/admin/guesthouses/{guesthouseId}/room-types", guesthouseId()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createsRoomTypeWithEditableQuantityAndPublishesIt() throws Exception {
    UUID guesthouseId = guesthouseId();
    mockMvc
        .perform(
            post("/api/admin/guesthouses/{guesthouseId}/room-types", guesthouseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request("family", 7, 4, true)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value("family"))
        .andExpect(jsonPath("$.quantity").value(7))
        .andExpect(jsonPath("$.standardOccupancy").value(4));

    mockMvc
        .perform(get("/api/guesthouses/nisztor-panzio").queryParam("lang", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roomTypes[?(@.name == 'Family room')].quantity[0]").value(7));
  }

  private UUID guesthouseId() {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM guesthouse WHERE slug = 'nisztor-panzio'", UUID.class);
  }

  private String request(String code, int quantity, int occupancy, boolean active) {
    return """
        {
          "code":"%s", "quantity":%d, "standardOccupancy":%d,
          "roomsWithExtraBed":0, "extraBedsPerEligibleRoom":0, "active":%s,
          "translations":[
            {"language":"hu", "name":"Családi szoba", "shortDescription":"Tágas családi szoba.", "detailedDescription":""},
            {"language":"ro", "name":"Cameră de familie", "shortDescription":"Cameră spațioasă pentru familie.", "detailedDescription":""},
            {"language":"en", "name":"Family room", "shortDescription":"Spacious family room.", "detailedDescription":""}
          ]
        }
        """
        .formatted(code, quantity, occupancy, active);
  }
}
