package com.bukovina.platform.accommodation.booking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ValidatedBooking(
    UUID guesthouseId,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    long nights,
    int adults,
    int childrenAge3to10,
    int childrenAge0to3,
    int totalGuests,
    int breakfastParticipants,
    int dinnerParticipants,
    int selectedRoomCount,
    int selectedCapacity,
    int singleRoomCount,
    List<ValidatedRoomSelection> roomSelections) {

  public record ValidatedRoomSelection(UUID roomTypeId, int quantity, int standardOccupancy) {}
}
