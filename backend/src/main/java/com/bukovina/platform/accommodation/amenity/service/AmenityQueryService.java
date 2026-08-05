package com.bukovina.platform.accommodation.amenity.service;

import com.bukovina.platform.accommodation.amenity.dao.AmenityQueryDao;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AmenityQueryService implements AmenityQuery {

  private final AmenityQueryDao queryDao;

  public AmenityQueryService(AmenityQueryDao queryDao) {
    this.queryDao = queryDao;
  }

  @Override
  public List<AmenityView> findPublished(UUID guesthouseId, String language) {
    return queryDao.findPublished(guesthouseId, language);
  }
}
