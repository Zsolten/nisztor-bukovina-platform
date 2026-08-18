package com.bukovina.platform.accommodation.amenity.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record AdminAmenityOrderUpdateRequest(@NotNull List<UUID> amenityIds) {}
