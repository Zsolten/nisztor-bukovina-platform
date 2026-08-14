package com.bukovina.platform.accommodation.amenity.controller;

import com.bukovina.platform.accommodation.amenity.dto.AdminAmenityErrorResponse;
import com.bukovina.platform.accommodation.amenity.service.AdminAmenityException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AdminAmenityController.class)
public class AdminAmenityExceptionHandler {

  @ExceptionHandler(AdminAmenityException.class)
  ResponseEntity<AdminAmenityErrorResponse> amenityError(AdminAmenityException exception) {
    String[] parts = exception.getCode().split(":", 3);
    String code = parts[0];
    Map<String, String> errors = parts.length == 3 ? Map.of(parts[1], parts[2]) : Map.of();
    HttpStatus status =
        switch (code) {
          case "ADMIN_AMENITY_NOT_FOUND", "ADMIN_AMENITY_GUESTHOUSE_NOT_FOUND" ->
              HttpStatus.NOT_FOUND;
          case "AMENITY_CODE_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
          default -> HttpStatus.BAD_REQUEST;
        };
    return response(status, code, errors);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<AdminAmenityErrorResponse> invalidRequest(
      MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(error.getField(), "INVALID");
    }
    return response(HttpStatus.BAD_REQUEST, "ADMIN_AMENITY_VALIDATION_FAILED", errors);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<AdminAmenityErrorResponse> malformedRequest() {
    return response(HttpStatus.BAD_REQUEST, "INVALID_ADMIN_AMENITY_REQUEST", Map.of());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<AdminAmenityErrorResponse> integrityViolation() {
    return response(HttpStatus.CONFLICT, "AMENITY_CODE_ALREADY_EXISTS", Map.of());
  }

  private ResponseEntity<AdminAmenityErrorResponse> response(
      HttpStatus status, String code, Map<String, String> fieldErrors) {
    return ResponseEntity.status(status).body(new AdminAmenityErrorResponse(code, fieldErrors));
  }
}
