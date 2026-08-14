package com.bukovina.platform.accommodation.amenity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

public record AdminAmenityAssignmentUpdateRequest(
    @NotNull UUID guesthouseId, @NotNull Boolean active, @PositiveOrZero Integer displayOrder) {}
