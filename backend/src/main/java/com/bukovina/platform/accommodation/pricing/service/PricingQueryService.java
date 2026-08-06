package com.bukovina.platform.accommodation.pricing.service;

import com.bukovina.platform.accommodation.pricing.dao.PricingQueryDao;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PricingQueryService implements PricingQuery {

  private final PricingQueryDao queryDao;

  public PricingQueryService(PricingQueryDao queryDao) {
    this.queryDao = queryDao;
  }

  @Override
  public PricingView findPublished(UUID guesthouseId, String language) {
    return queryDao.findPublished(guesthouseId, language);
  }
}
