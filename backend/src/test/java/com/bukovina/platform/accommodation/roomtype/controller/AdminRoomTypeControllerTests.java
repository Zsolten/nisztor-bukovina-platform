package com.bukovina.platform.accommodation.roomtype.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import(PostgreSqlTestContainerConfiguration.class)
@Transactional
class AdminRoomTypeControllerTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();

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

    MvcResult publicResponse =
        mockMvc
            .perform(get("/api/guesthouses/nisztor-panzio").queryParam("lang", "en"))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode roomTypes =
        objectMapper.readTree(publicResponse.getResponse().getContentAsString()).at("/roomTypes");
    for (JsonNode roomType : roomTypes) {
      if ("Family room".equals(roomType.path("name").asText())) {
        assertEquals(7, roomType.path("quantity").asInt());
        return;
      }
    }
    throw new AssertionError("Family room not found in public response");
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void rejectsNullRoomTypeIdInOrderWithStableValidationError() throws Exception {
    mockMvc
        .perform(
            put("/api/admin/guesthouses/{guesthouseId}/room-types/order", guesthouseId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomTypeIds\":[null]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ADMIN_ROOM_TYPE_VALIDATION_FAILED"));
  }

  private UUID guesthouseId() {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM guesthouse WHERE slug = 'nisztor-panzio'", UUID.class);
  }

  private String request(String code, int quantity, int occupancy, boolean active) {
    return """
        {
          "code":"%s", "quantity":%d, "standardOccupancy":%d,
          "active":%s,
          "translations":[
            {"language":"hu", "name":"Családi szoba", "shortDescription":"Tágas családi szoba."},
            {"language":"ro", "name":"Cameră de familie", "shortDescription":"Cameră spațioasă pentru familie."},
            {"language":"en", "name":"Family room", "shortDescription":"Spacious family room."}
          ]
        }
        """
        .formatted(code, quantity, occupancy, active);
  }
}
