package com.bukovina.platform.accommodation.guesthouse.dto;

import java.util.List;
import java.util.UUID;

public record RoomTypeResponse(
    UUID id,
    String name,
    String shortDescription,
    int quantity,
    int standardOccupancy,
    List<String> features) {}
