package com.bukovina.platform.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Provides a stable JSON response when no more specific API endpoint matches the request. */
@RestController
public class ApiFallbackController {

  @RequestMapping("/api/**")
  ResponseEntity<ApiEndpointNotFoundResponse> endpointNotFound(HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            new ApiEndpointNotFoundResponse(
                HttpStatus.NOT_FOUND.value(), "API_ENDPOINT_NOT_FOUND", request.getRequestURI()));
  }
}
