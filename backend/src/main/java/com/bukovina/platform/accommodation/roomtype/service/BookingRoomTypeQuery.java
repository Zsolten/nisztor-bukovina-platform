package com.bukovina.platform.accommodation.roomtype.service;

import java.util.Optional;
import java.util.UUID;

public interface BookingRoomTypeQuery {

  Optional<BookableRoomTypeView> findById(UUID roomTypeId);
}
