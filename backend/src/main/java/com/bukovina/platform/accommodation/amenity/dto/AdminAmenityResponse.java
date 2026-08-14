package com.bukovina.platform.accommodation.amenity.dto;

import java.util.List;
import java.util.UUID;

public record AdminAmenityResponse(
    UUID id,
    String code,
    String category,
    String pricingType,
    List<AdminAmenityTranslationResponse> translations,
    List<AdminAmenityAssignmentResponse> assignments) {}
