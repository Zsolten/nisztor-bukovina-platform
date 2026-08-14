package com.bukovina.platform.accommodation.amenity.dto;

import jakarta.validation.constraints.Size;

public record AdminAmenityTranslationUpdateRequest(
    String language,
    @Size(max = 240) String name,
    @Size(max = 1000) String description,
    @Size(max = 5000) String detailedDescription) {}
