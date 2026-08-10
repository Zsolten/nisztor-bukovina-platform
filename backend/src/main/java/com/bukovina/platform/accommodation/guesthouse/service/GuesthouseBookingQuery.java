package com.bukovina.platform.accommodation.guesthouse.service;

import java.util.UUID;

public interface GuesthouseBookingQuery {

  boolean existsActive(UUID guesthouseId);
}
