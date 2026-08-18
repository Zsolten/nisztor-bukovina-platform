package com.bukovina.platform.tourism.startour.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = StarTourController.class)
class StarTourExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<StarTourErrorResponse> requestError(ResponseStatusException exception) {
    String code =
        exception.getReason() == null ? "STAR_TOUR_REQUEST_FAILED" : exception.getReason();
    return ResponseEntity.status(exception.getStatusCode()).body(new StarTourErrorResponse(code));
  }

  record StarTourErrorResponse(String code) {}
}
