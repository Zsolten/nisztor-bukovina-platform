package com.bukovina.platform.tourism.routing;

import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class StarTourRouteCacheInvalidator {
  private final JdbcClient jdbc;

  public StarTourRouteCacheInvalidator(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Removes cached complete routes that contain an attraction whose coordinates changed. */
  public void invalidateForAttraction(UUID attractionId) {
    jdbc.sql(
            "DELETE FROM star_tour_route_variant variant USING star_tour_attraction assignment "
                + "WHERE variant.star_tour_id = assignment.star_tour_id "
                + "AND assignment.attraction_id = :attractionId")
        .param("attractionId", attractionId)
        .update();
  }
}
