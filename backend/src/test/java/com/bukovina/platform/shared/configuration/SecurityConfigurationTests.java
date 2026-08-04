package com.bukovina.platform.shared.configuration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import({PostgreSqlTestContainerConfiguration.class, SecurityConfigurationTests.TestEndpoint.class})
class SecurityConfigurationTests {

  @Autowired private MockMvc mockMvc;

  @Test
  void allowsUnauthenticatedGetRequests() throws Exception {
    mockMvc.perform(get("/__test/open")).andExpect(status().isOk());
  }

  @Test
  void allowsUnauthenticatedPostRequests() throws Exception {
    mockMvc.perform(post("/__test/open")).andExpect(status().isOk());
  }

  @Test
  void exposesOnlyAHealthyActuatorFoundation() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
    mockMvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
  }

  @RestController
  static class TestEndpoint {

    @GetMapping("/__test/open")
    String get() {
      return "open";
    }

    @PostMapping("/__test/open")
    String post() {
      return "open";
    }
  }
}
