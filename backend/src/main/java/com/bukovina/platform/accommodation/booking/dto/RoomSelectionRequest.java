package com.bukovina.platform.accommodation.booking.dto;

import java.util.UUID;

public record RoomSelectionRequest(UUID roomTypeId, Integer quantity) {}
