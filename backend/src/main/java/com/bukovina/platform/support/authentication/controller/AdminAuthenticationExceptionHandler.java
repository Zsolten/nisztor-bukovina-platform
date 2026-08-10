package com.bukovina.platform.support.authentication.controller;

import com.bukovina.platform.support.authentication.AdminAuthenticationException;
import com.bukovina.platform.support.authentication.dto.AdminAuthenticationErrorResponse;
import org.springframework.http.HttpStatus;
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
}
