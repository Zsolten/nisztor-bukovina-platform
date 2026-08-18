package com.bukovina.platform.tourism.routing;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tourism.routing.base")
public record TourismRouteBaseProperties(BigDecimal latitude, BigDecimal longitude) {

  public TourismRouteBaseProperties {
    if (latitude == null || longitude == null) {
      throw new IllegalArgumentException("TOURISM_ROUTE_BASE_COORDINATES_REQUIRED");
    }
  }
}
