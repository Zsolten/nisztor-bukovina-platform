package com.bukovina.platform.accommodation.roomtype.service;

import java.util.List;
import java.util.UUID;

public interface RoomTypeQuery {

  List<RoomTypeView> findPublished(UUID guesthouseId, String language);
}
