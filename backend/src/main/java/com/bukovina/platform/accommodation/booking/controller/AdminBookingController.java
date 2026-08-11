package com.bukovina.platform.accommodation.booking.controller;

import com.bukovina.platform.accommodation.booking.dto.AdminBookingDetailResponse;
import com.bukovina.platform.accommodation.booking.dto.AdminBookingInternalNoteUpdateRequest;
import com.bukovina.platform.accommodation.booking.dto.AdminBookingPageResponse;
import com.bukovina.platform.accommodation.booking.dto.AdminBookingStatusUpdateRequest;
import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import com.bukovina.platform.accommodation.booking.service.AdminBookingQueryService;
import com.bukovina.platform.accommodation.booking.service.AdminBookingWorkflowService;
import com.bukovina.platform.accommodation.booking.service.AdminBookingWorkflowValidationException;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

  private final AdminBookingQueryService queryService;
  private final AdminBookingWorkflowService workflowService;

  public AdminBookingController(
      AdminBookingQueryService queryService, AdminBookingWorkflowService workflowService) {
    this.queryService = queryService;
    this.workflowService = workflowService;
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

  @PatchMapping("/{bookingId}/status")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changeStatus(
      @PathVariable UUID bookingId,
      @RequestBody AdminBookingStatusUpdateRequest request,
      Authentication authentication) {
    workflowService.changeStatus(
        bookingId, request == null ? null : request.status(), adminActor(authentication));
  }

  @PatchMapping("/{bookingId}/internal-note")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateInternalNote(
      @PathVariable UUID bookingId, @RequestBody AdminBookingInternalNoteUpdateRequest request) {
    if (request == null || request.internalNote() == null) {
      throw new AdminBookingWorkflowValidationException("INTERNAL_NOTE_REQUIRED");
    }
    workflowService.updateInternalNote(bookingId, request.internalNote());
  }

  private String adminActor(Authentication authentication) {
    return "ADMIN:" + authentication.getName();
  }
}
