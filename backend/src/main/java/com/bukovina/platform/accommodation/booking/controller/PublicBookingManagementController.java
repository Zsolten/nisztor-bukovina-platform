package com.bukovina.platform.accommodation.booking.controller;

import com.bukovina.platform.accommodation.booking.dto.BookingManagementResponse;
import com.bukovina.platform.accommodation.booking.service.BookingManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/booking-management/{token}")
public class PublicBookingManagementController {

  private final BookingManagementService service;

  public PublicBookingManagementController(BookingManagementService service) {
    this.service = service;
  }

  @GetMapping
  public BookingManagementResponse get(
      @PathVariable String token, @RequestParam(defaultValue = "hu") String lang) {
    return service.get(token, lang);
  }

  @PostMapping("/cancellation")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancel(@PathVariable String token) {
    service.cancel(token);
  }
}
