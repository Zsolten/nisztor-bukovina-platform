package com.bukovina.platform.accommodation.roomtype.service;

import com.bukovina.platform.accommodation.roomtype.dao.RoomTypeQueryDao;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RoomTypeQueryService implements RoomTypeQuery {

  private final RoomTypeQueryDao queryDao;

  public RoomTypeQueryService(RoomTypeQueryDao queryDao) {
    this.queryDao = queryDao;
  }

  @Override
  public List<RoomTypeView> findPublished(UUID guesthouseId, String language) {
    return queryDao.findPublished(guesthouseId, language);
  }
}
