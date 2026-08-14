package com.bukovina.platform.accommodation.guesthouse.service;

import java.util.UUID;

public interface GuesthouseExistenceQuery {

  boolean exists(UUID guesthouseId);
}
