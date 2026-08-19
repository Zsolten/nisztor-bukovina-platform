package com.bukovina.platform.accommodation.booking.controller;

import com.bukovina.platform.accommodation.booking.dto.BookingErrorResponse;
import com.bukovina.platform.accommodation.booking.dto.BookingFieldErrorResponse;
import com.bukovina.platform.accommodation.booking.service.BookingConflictException;
import com.bukovina.platform.accommodation.booking.service.BookingValidationException;
import com.bukovina.platform.accommodation.booking.service.PublicBookingRateLimitException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PublicBookingController.class)
public class BookingExceptionHandler {

  @ExceptionHandler(BookingValidationException.class)
  ResponseEntity<BookingErrorResponse> validation(BookingValidationException exception) {
    List<BookingFieldErrorResponse> errors =
        exception.getProblems().stream()
            .map(
                problem ->
                    new BookingFieldErrorResponse(problem.code(), problem.field(), problem.rule()))
            .toList();
    return ResponseEntity.badRequest()
        .body(new BookingErrorResponse("BOOKING_VALIDATION_FAILED", errors, null));
  }

  @ExceptionHandler(BookingConflictException.class)
  ResponseEntity<BookingErrorResponse> conflict(BookingConflictException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            new BookingErrorResponse(exception.getCode(), List.of(), exception.getCurrentQuote()));
  }

  @ExceptionHandler(PublicBookingRateLimitException.class)
  ResponseEntity<BookingErrorResponse> rateLimited(PublicBookingRateLimitException exception) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header(HttpHeaders.RETRY_AFTER, Long.toString(exception.getRetryAfterSeconds()))
        .body(new BookingErrorResponse("BOOKING_RATE_LIMITED", List.of(), null));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<BookingErrorResponse> malformedRequest() {
    return ResponseEntity.badRequest()
        .body(
            new BookingErrorResponse(
                "INVALID_REQUEST",
                List.of(new BookingFieldErrorResponse("INVALID_REQUEST", "request", "validJson")),
                null));
  }
}
