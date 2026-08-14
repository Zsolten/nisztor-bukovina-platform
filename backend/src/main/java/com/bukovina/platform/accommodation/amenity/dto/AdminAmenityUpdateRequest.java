package com.bukovina.platform.accommodation.amenity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdminAmenityUpdateRequest(
    @NotBlank @Size(max = 80) @Pattern(regexp = "[a-z0-9]+(?:_[a-z0-9]+)*") String code,
    @NotBlank String category,
    @NotBlank String pricingType,
    @NotNull @Size(min = 3, max = 3) List<@Valid AdminAmenityTranslationUpdateRequest> translations,
    @NotNull List<@Valid AdminAmenityAssignmentUpdateRequest> assignments) {}
