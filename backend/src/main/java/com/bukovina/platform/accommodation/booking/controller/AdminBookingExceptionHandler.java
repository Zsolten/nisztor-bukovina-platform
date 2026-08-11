package com.bukovina.platform.accommodation.booking.controller;

import com.bukovina.platform.accommodation.booking.dto.AdminBookingErrorResponse;
import com.bukovina.platform.accommodation.booking.service.AdminBookingNotFoundException;
import com.bukovina.platform.accommodation.booking.service.AdminBookingQueryValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AdminBookingController.class)
public class AdminBookingExceptionHandler {

  @ExceptionHandler(AdminBookingNotFoundException.class)
  ResponseEntity<AdminBookingErrorResponse> notFound() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new AdminBookingErrorResponse("ADMIN_BOOKING_NOT_FOUND"));
  }

  @ExceptionHandler(AdminBookingQueryValidationException.class)
  ResponseEntity<AdminBookingErrorResponse> invalidQuery() {
    return ResponseEntity.badRequest()
        .body(new AdminBookingErrorResponse("INVALID_ADMIN_BOOKING_QUERY"));
  }
}
