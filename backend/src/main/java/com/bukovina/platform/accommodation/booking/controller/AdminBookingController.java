package com.bukovina.platform.accommodation.booking.controller;

import com.bukovina.platform.accommodation.booking.dto.AdminBookingDetailResponse;
import com.bukovina.platform.accommodation.booking.dto.AdminBookingPageResponse;
import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import com.bukovina.platform.accommodation.booking.service.AdminBookingQueryService;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

  private final AdminBookingQueryService queryService;

  public AdminBookingController(AdminBookingQueryService queryService) {
    this.queryService = queryService;
  }

  @GetMapping
  public AdminBookingPageResponse list(
      @RequestParam(required = false) UUID guesthouseId,
      @RequestParam(required = false) BookingStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate createdFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate createdTo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return queryService.list(guesthouseId, status, createdFrom, createdTo, page, size);
  }

  @GetMapping("/{bookingId}")
  public AdminBookingDetailResponse detail(@PathVariable UUID bookingId) {
    return queryService.detail(bookingId);
  }
}
