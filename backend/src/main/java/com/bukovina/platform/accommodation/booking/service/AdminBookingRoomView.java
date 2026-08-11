package com.bukovina.platform.accommodation.booking.service;

import java.util.UUID;

public record AdminBookingRoomView(UUID roomTypeId, String roomTypeName, int quantity) {}
