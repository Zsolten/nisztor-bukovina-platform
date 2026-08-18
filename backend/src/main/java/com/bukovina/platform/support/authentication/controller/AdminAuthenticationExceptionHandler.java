package com.bukovina.platform.support.authentication.controller;

import com.bukovina.platform.support.authentication.AdminAuthenticationException;
import com.bukovina.platform.support.authentication.AdminLoginRateLimitException;
import com.bukovina.platform.support.authentication.dto.AdminAuthenticationErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AdminAuthController.class)
public class AdminAuthenticationExceptionHandler {

  @ExceptionHandler(AdminAuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  AdminAuthenticationErrorResponse handleInvalidCredentials() {
    return new AdminAuthenticationErrorResponse("INVALID_ADMIN_CREDENTIALS");
  }

  @ExceptionHandler(AdminLoginRateLimitException.class)
  ResponseEntity<AdminAuthenticationErrorResponse> handleRateLimit(
      AdminLoginRateLimitException exception) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header(HttpHeaders.RETRY_AFTER, Long.toString(exception.getRetryAfterSeconds()))
        .body(new AdminAuthenticationErrorResponse("ADMIN_LOGIN_RATE_LIMITED"));
  }
}
