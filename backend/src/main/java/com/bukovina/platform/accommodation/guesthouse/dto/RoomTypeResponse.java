package com.bukovina.platform.accommodation.guesthouse.dto;

import java.util.List;

public record RoomTypeResponse(
    String id,
    String name,
    int quantity,
    int standardOccupancy,
    int roomsWithExtraBed,
    int extraBedsPerEligibleRoom,
    List<String> features) {}
