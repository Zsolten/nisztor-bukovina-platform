package com.bukovina.platform.accommodation.pricing.controller;

import com.bukovina.platform.accommodation.pricing.dto.AdminGuesthousePricingResponse;
import com.bukovina.platform.accommodation.pricing.dto.AdminGuesthousePricingUpdateRequest;
import com.bukovina.platform.accommodation.pricing.service.AdminGuesthousePricingService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/guesthouses/{guesthouseId}/pricing")
public class AdminGuesthousePricingController {

  private final AdminGuesthousePricingService pricingService;

  public AdminGuesthousePricingController(AdminGuesthousePricingService pricingService) {
    this.pricingService = pricingService;
  }

  @GetMapping
  public AdminGuesthousePricingResponse find(@PathVariable UUID guesthouseId) {
    return pricingService.find(guesthouseId);
  }

  @PutMapping
  public AdminGuesthousePricingResponse update(
      @PathVariable UUID guesthouseId,
      @Valid @RequestBody AdminGuesthousePricingUpdateRequest request) {
    return pricingService.update(guesthouseId, request);
  }
}
