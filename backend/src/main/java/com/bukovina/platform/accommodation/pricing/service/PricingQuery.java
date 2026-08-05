package com.bukovina.platform.accommodation.pricing.service;

import java.util.UUID;

public interface PricingQuery {

  PricingView findPublished(UUID guesthouseId, String language);
}
