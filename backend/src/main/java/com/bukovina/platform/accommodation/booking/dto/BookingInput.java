package com.bukovina.platform.accommodation.booking.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingInput {

  UUID guesthouseId();

  LocalDate checkInDate();

  LocalDate checkOutDate();

  Integer adults();

  Integer childrenAge3to10();

  Integer childrenAge0to3();

  List<RoomSelectionRequest> roomSelections();

  BookingServicesRequest services();
}
