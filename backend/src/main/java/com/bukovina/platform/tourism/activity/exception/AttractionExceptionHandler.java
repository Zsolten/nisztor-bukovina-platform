package com.bukovina.platform.tourism.activity.exception;

import com.bukovina.platform.tourism.activity.controller.AttractionController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AttractionController.class)
class AttractionExceptionHandler {
  @ExceptionHandler(AttractionException.class)
  ResponseEntity<AttractionErrorResponse> requestError(AttractionException exception) {
    return ResponseEntity.status(exception.status())
        .body(new AttractionErrorResponse(exception.code()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<AttractionErrorResponse> malformedRequest() {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new AttractionErrorResponse("INVALID_ADMIN_ATTRACTION_REQUEST"));
  }

  record AttractionErrorResponse(String code) {}
}
