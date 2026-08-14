package com.bukovina.platform.accommodation.guesthouse.dto;

public record AmenityResponse(
    String id,
    String name,
    String description,
    String detailedDescription,
    String category,
    String pricingType,
    int displayOrder) {}
