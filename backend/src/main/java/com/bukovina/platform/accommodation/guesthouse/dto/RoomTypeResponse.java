package com.bukovina.platform.accommodation.guesthouse.dto;

import java.util.List;
import java.util.UUID;

public record RoomTypeResponse(
    UUID id,
    String name,
    String shortDescription,
    String detailedDescription,
    int quantity,
    int standardOccupancy,
    int roomsWithExtraBed,
    int extraBedsPerEligibleRoom,
    List<String> features) {}
