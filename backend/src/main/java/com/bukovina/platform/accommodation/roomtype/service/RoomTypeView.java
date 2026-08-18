package com.bukovina.platform.accommodation.roomtype.service;

import java.util.List;
import java.util.UUID;

public record RoomTypeView(
    UUID id,
    String name,
    String shortDescription,
    int quantity,
    int standardOccupancy,
    List<String> features) {}
