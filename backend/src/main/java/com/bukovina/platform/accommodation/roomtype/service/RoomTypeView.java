package com.bukovina.platform.accommodation.roomtype.service;

import java.util.List;
import java.util.UUID;

public record RoomTypeView(
    UUID id,
    String name,
    int quantity,
    int standardOccupancy,
    int roomsWithExtraBed,
    int extraBedsPerEligibleRoom,
    List<String> features) {}
