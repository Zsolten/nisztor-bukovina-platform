package com.bukovina.platform.accommodation.guesthouse.controller;

import com.bukovina.platform.accommodation.guesthouse.dto.AdminGuesthouseContentErrorResponse;
import com.bukovina.platform.accommodation.guesthouse.service.AdminGuesthouseContentConflictException;
import com.bukovina.platform.accommodation.guesthouse.service.AdminGuesthouseContentNotFoundException;
import com.bukovina.platform.accommodation.guesthouse.service.AdminGuesthouseContentValidationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AdminGuesthouseContentController.class)
public class AdminGuesthouseContentExceptionHandler {

  @ExceptionHandler(AdminGuesthouseContentNotFoundException.class)
  ResponseEntity<AdminGuesthouseContentErrorResponse> notFound() {
    return response(HttpStatus.NOT_FOUND, "ADMIN_GUESTHOUSE_NOT_FOUND", Map.of(), null);
  }

  @ExceptionHandler(AdminGuesthouseContentValidationException.class)
  ResponseEntity<AdminGuesthouseContentErrorResponse> invalidLanguage(
      AdminGuesthouseContentValidationException exception) {
    return response(HttpStatus.BAD_REQUEST, exception.getCode(), Map.of(), null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<AdminGuesthouseContentErrorResponse> invalidContent(
      MethodArgumentNotValidException exception) {
    Map<String, String> fieldErrors = new LinkedHashMap<>();
    exception.getBindingResult().getFieldErrors().stream()
        .forEach(error -> fieldErrors.putIfAbsent(error.getField(), errorCode(error)));
    return response(HttpStatus.BAD_REQUEST, "ADMIN_CONTENT_VALIDATION_FAILED", fieldErrors, null);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<AdminGuesthouseContentErrorResponse> malformedContent() {
    return response(HttpStatus.BAD_REQUEST, "INVALID_ADMIN_CONTENT_REQUEST", Map.of(), null);
  }

  @ExceptionHandler(AdminGuesthouseContentConflictException.class)
  ResponseEntity<AdminGuesthouseContentErrorResponse> conflict(
      AdminGuesthouseContentConflictException exception) {
    return response(
        HttpStatus.CONFLICT,
        "ADMIN_CONTENT_VERSION_CONFLICT",
        Map.of(),
        exception.getCurrentContent());
  }

  private String errorCode(FieldError error) {
    return error.getCodes() == null
            || java.util.Arrays.stream(error.getCodes())
                .noneMatch(code -> code.startsWith("NotBlank"))
        ? "TOO_LONG"
        : "REQUIRED";
  }

  private ResponseEntity<AdminGuesthouseContentErrorResponse> response(
      HttpStatus status,
      String code,
      Map<String, String> fieldErrors,
      com.bukovina.platform.accommodation.guesthouse.dto.AdminGuesthouseTranslationResponse
          currentContent) {
    return ResponseEntity.status(status)
        .body(new AdminGuesthouseContentErrorResponse(code, fieldErrors, currentContent));
  }
}
