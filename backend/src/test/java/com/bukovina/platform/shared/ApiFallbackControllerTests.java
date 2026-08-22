package com.bukovina.platform.shared;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.shared.exception.ApiFallbackController;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiFallbackControllerTests {

  private final MockMvc mockMvc =
      MockMvcBuilders.standaloneSetup(new ApiFallbackController()).build();

  @Test
  void returnsAStableJsonResponseForUnknownApiEndpoints() throws Exception {
    mockMvc
        .perform(get("/api/tourism/unknown/route"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.code").value("API_ENDPOINT_NOT_FOUND"))
        .andExpect(jsonPath("$.path").value("/api/tourism/unknown/route"));
  }
}
