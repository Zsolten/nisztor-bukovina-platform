package com.bukovina.platform.accommodation.booking.controller;

import com.bukovina.platform.accommodation.booking.dto.BookingQuoteRequest;
import com.bukovina.platform.accommodation.booking.dto.BookingQuoteResponse;
import com.bukovina.platform.accommodation.booking.dto.BookingRequestCreatedResponse;
import com.bukovina.platform.accommodation.booking.dto.CreateBookingRequest;
import com.bukovina.platform.accommodation.booking.service.BookingQuoteService;
import com.bukovina.platform.accommodation.booking.service.BookingRequestService;
import com.bukovina.platform.accommodation.booking.service.PublicBookingRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
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
  private final PublicBookingRateLimiter rateLimiter;

  public PublicBookingController(
      BookingQuoteService quoteService,
      BookingRequestService requestService,
      PublicBookingRateLimiter rateLimiter) {
    this.quoteService = quoteService;
    this.requestService = requestService;
    this.rateLimiter = rateLimiter;
  }

  @PostMapping("/api/booking-quotes")
  public BookingQuoteResponse quote(
      @RequestBody BookingQuoteRequest request, HttpServletRequest httpRequest) {
    rateLimiter.consumeQuote(httpRequest.getRemoteAddr());
    return quoteService.quote(request);
  }

  @PostMapping("/api/booking-requests")
  @ResponseStatus(HttpStatus.CREATED)
  public BookingRequestCreatedResponse create(
      @RequestBody CreateBookingRequest request,
      @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
      HttpServletRequest httpRequest) {
    rateLimiter.consumeRequest(httpRequest.getRemoteAddr(), request.contactEmail());
    return requestService.create(request, idempotencyKey);
  }
}
