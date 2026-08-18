package com.bukovina.platform.accommodation.roomtype.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record AdminRoomTypeOrderUpdateRequest(@NotNull List<@NotNull UUID> roomTypeIds) {}
