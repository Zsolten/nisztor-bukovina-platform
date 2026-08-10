package com.bukovina.platform.accommodation.booking.controller;

import com.bukovina.platform.accommodation.booking.dto.BookingQuoteRequest;
import com.bukovina.platform.accommodation.booking.dto.BookingQuoteResponse;
import com.bukovina.platform.accommodation.booking.dto.BookingRequestCreatedResponse;
import com.bukovina.platform.accommodation.booking.dto.CreateBookingRequest;
import com.bukovina.platform.accommodation.booking.service.BookingQuoteService;
import com.bukovina.platform.accommodation.booking.service.BookingRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicBookingController {

  private final BookingQuoteService quoteService;
  private final BookingRequestService requestService;

  public PublicBookingController(
      BookingQuoteService quoteService, BookingRequestService requestService) {
    this.quoteService = quoteService;
    this.requestService = requestService;
  }

  @PostMapping("/api/booking-quotes")
  public BookingQuoteResponse quote(@RequestBody BookingQuoteRequest request) {
    return quoteService.quote(request);
  }

  @PostMapping("/api/booking-requests")
  @ResponseStatus(HttpStatus.CREATED)
  public BookingRequestCreatedResponse create(
      @RequestBody CreateBookingRequest request,
      @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
    return requestService.create(request, idempotencyKey);
  }
}
