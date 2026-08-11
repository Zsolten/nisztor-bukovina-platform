package com.bukovina.platform.accommodation.booking.controller;

import com.bukovina.platform.accommodation.booking.dto.AdminBookingErrorResponse;
import com.bukovina.platform.accommodation.booking.dto.AdminBookingWorkflowErrorResponse;
import com.bukovina.platform.accommodation.booking.model.InvalidBookingStatusTransitionException;
import com.bukovina.platform.accommodation.booking.service.AdminBookingNotFoundException;
import com.bukovina.platform.accommodation.booking.service.AdminBookingQueryValidationException;
import com.bukovina.platform.accommodation.booking.service.AdminBookingWorkflowValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = AdminBookingController.class)
public class AdminBookingExceptionHandler {

  @ExceptionHandler(AdminBookingNotFoundException.class)
  ResponseEntity<AdminBookingErrorResponse> notFound() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new AdminBookingErrorResponse("ADMIN_BOOKING_NOT_FOUND"));
  }

  @ExceptionHandler({
    AdminBookingQueryValidationException.class,
    MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<AdminBookingErrorResponse> invalidQuery() {
    return ResponseEntity.badRequest()
        .body(new AdminBookingErrorResponse("INVALID_ADMIN_BOOKING_QUERY"));
  }

  @ExceptionHandler(InvalidBookingStatusTransitionException.class)
  ResponseEntity<AdminBookingWorkflowErrorResponse> invalidTransition(
      InvalidBookingStatusTransitionException exception) {
    return ResponseEntity.badRequest()
        .body(
            new AdminBookingWorkflowErrorResponse(
                "INVALID_BOOKING_STATUS_TRANSITION",
                exception.getCurrentStatus(),
                exception.getRequestedStatus()));
  }

  @ExceptionHandler(AdminBookingWorkflowValidationException.class)
  ResponseEntity<AdminBookingWorkflowErrorResponse> invalidWorkflowRequest(
      AdminBookingWorkflowValidationException exception) {
    return ResponseEntity.badRequest()
        .body(new AdminBookingWorkflowErrorResponse(exception.getCode(), null, null));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<AdminBookingWorkflowErrorResponse> malformedWorkflowRequest() {
    return ResponseEntity.badRequest()
        .body(new AdminBookingWorkflowErrorResponse("INVALID_ADMIN_BOOKING_REQUEST", null, null));
  }
}
