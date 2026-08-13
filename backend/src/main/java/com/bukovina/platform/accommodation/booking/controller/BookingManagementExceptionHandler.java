package com.bukovina.platform.accommodation.booking.controller;

import com.bukovina.platform.accommodation.booking.dto.BookingManagementErrorResponse;
import com.bukovina.platform.accommodation.booking.service.BookingCancellationNotAllowedException;
import com.bukovina.platform.accommodation.booking.service.BookingManagementNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PublicBookingManagementController.class)
public class BookingManagementExceptionHandler {

  @ExceptionHandler(BookingManagementNotFoundException.class)
  ResponseEntity<BookingManagementErrorResponse> notFound() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new BookingManagementErrorResponse("BOOKING_MANAGEMENT_LINK_INVALID"));
  }

  @ExceptionHandler(BookingCancellationNotAllowedException.class)
  ResponseEntity<BookingManagementErrorResponse> cancellationNotAllowed() {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new BookingManagementErrorResponse("BOOKING_CANCELLATION_NOT_ALLOWED"));
  }
}
