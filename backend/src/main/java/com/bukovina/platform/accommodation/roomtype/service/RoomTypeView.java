package com.bukovina.platform.accommodation.roomtype.service;

import java.util.List;

public record RoomTypeView(
    String id,
    String name,
    int quantity,
    int standardOccupancy,
    int roomsWithExtraBed,
    int extraBedsPerEligibleRoom,
    List<String> features) {}
