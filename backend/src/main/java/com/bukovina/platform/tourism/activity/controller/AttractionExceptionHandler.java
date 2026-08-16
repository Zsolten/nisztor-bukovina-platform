package com.bukovina.platform.tourism.activity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = AttractionController.class)
class AttractionExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<AttractionErrorResponse> requestError(ResponseStatusException exception) {
    String code =
        exception.getReason() == null ? "ATTRACTION_REQUEST_FAILED" : exception.getReason();
    return ResponseEntity.status(exception.getStatusCode()).body(new AttractionErrorResponse(code));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<AttractionErrorResponse> malformedRequest() {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new AttractionErrorResponse("INVALID_ADMIN_ATTRACTION_REQUEST"));
  }

  record AttractionErrorResponse(String code) {}
}
