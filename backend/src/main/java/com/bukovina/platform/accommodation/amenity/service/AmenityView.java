package com.bukovina.platform.accommodation.amenity.service;

public record AmenityView(
    String id,
    String name,
    String description,
    String detailedDescription,
    String category,
    String pricingType) {}
