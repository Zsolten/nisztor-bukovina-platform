package com.bukovina.platform.accommodation.pricing.controller;

import com.bukovina.platform.accommodation.pricing.service.AdminPricingException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AdminGuesthousePricingController.class)
public class AdminGuesthousePricingExceptionHandler {

  @ExceptionHandler(AdminPricingException.class)
  ResponseEntity<AdminPricingErrorResponse> pricingError(AdminPricingException exception) {
    String[] parts = exception.getCode().split(":", 3);
    HttpStatus status =
        switch (parts[0]) {
          case "ADMIN_PRICING_GUESTHOUSE_NOT_FOUND", "ADMIN_PRICING_NOT_FOUND" ->
              HttpStatus.NOT_FOUND;
          default -> HttpStatus.BAD_REQUEST;
        };
    Map<String, String> fieldErrors = parts.length == 3 ? Map.of(parts[1], parts[2]) : Map.of();
    return ResponseEntity.status(status).body(new AdminPricingErrorResponse(parts[0], fieldErrors));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<AdminPricingErrorResponse> invalidFields(
      MethodArgumentNotValidException exception) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      fields.putIfAbsent(error.getField(), "INVALID");
    }
    return ResponseEntity.badRequest()
        .body(new AdminPricingErrorResponse("ADMIN_PRICING_VALIDATION_FAILED", fields));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<AdminPricingErrorResponse> invalidBody() {
    return ResponseEntity.badRequest()
        .body(new AdminPricingErrorResponse("INVALID_ADMIN_PRICING_REQUEST", Map.of()));
  }
}
