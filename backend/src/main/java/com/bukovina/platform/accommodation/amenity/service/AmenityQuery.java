package com.bukovina.platform.accommodation.amenity.service;

import java.util.List;
import java.util.UUID;

public interface AmenityQuery {

  List<AmenityView> findPublished(UUID guesthouseId, String language);
}
