package com.bukovina.platform.accommodation.amenity.controller;

import com.bukovina.platform.accommodation.amenity.dto.AdminAmenityOrderUpdateRequest;
import com.bukovina.platform.accommodation.amenity.dto.AdminAmenityResponse;
import com.bukovina.platform.accommodation.amenity.dto.AdminAmenityUpdateRequest;
import com.bukovina.platform.accommodation.amenity.service.AdminAmenityService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAmenityController {

  private final AdminAmenityService amenityService;

  public AdminAmenityController(AdminAmenityService amenityService) {
    this.amenityService = amenityService;
  }

  @GetMapping("/amenities")
  public List<AdminAmenityResponse> list() {
    return amenityService.list();
  }

  @PostMapping("/amenities")
  @ResponseStatus(HttpStatus.CREATED)
  public AdminAmenityResponse create(@Valid @RequestBody AdminAmenityUpdateRequest request) {
    return amenityService.create(request);
  }

  @PutMapping("/amenities/{amenityId}")
  public AdminAmenityResponse update(
      @PathVariable UUID amenityId, @Valid @RequestBody AdminAmenityUpdateRequest request) {
    return amenityService.update(amenityId, request);
  }

  @PutMapping("/guesthouses/{guesthouseId}/amenities/order")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reorder(
      @PathVariable UUID guesthouseId, @Valid @RequestBody AdminAmenityOrderUpdateRequest request) {
    amenityService.reorder(guesthouseId, request);
  }
}
