package com.bukovina.platform.accommodation.amenity.dto;

import java.util.UUID;

public record AdminAmenityAssignmentResponse(UUID guesthouseId, boolean active, int displayOrder) {}
