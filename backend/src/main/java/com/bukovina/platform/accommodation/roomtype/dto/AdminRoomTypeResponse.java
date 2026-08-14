package com.bukovina.platform.accommodation.roomtype.dto;

import java.util.List;
import java.util.UUID;

public record AdminRoomTypeResponse(
    UUID id,
    String code,
    int quantity,
    int standardOccupancy,
    int roomsWithExtraBed,
    int extraBedsPerEligibleRoom,
    boolean active,
    int displayOrder,
    List<AdminRoomTypeTranslationResponse> translations) {}
