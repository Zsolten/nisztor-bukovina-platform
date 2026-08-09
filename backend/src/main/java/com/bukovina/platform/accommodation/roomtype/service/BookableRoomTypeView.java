package com.bukovina.platform.accommodation.roomtype.service;

import java.util.UUID;

public record BookableRoomTypeView(
    UUID id, UUID guesthouseId, int quantity, int standardOccupancy, boolean active) {}
