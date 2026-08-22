package com.bukovina.platform.tourism.startour.exception;

import com.bukovina.platform.tourism.startour.controller.StarTourController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = StarTourController.class)
class StarTourExceptionHandler {
  @ExceptionHandler(StarTourException.class)
  ResponseEntity<StarTourErrorResponse> requestError(StarTourException exception) {
    return ResponseEntity.status(exception.status())
        .body(new StarTourErrorResponse(exception.code()));
  }

  record StarTourErrorResponse(String code) {}
}
