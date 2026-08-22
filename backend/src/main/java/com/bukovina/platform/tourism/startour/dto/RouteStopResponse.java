package com.bukovina.platform.tourism.startour.dto;

import java.math.BigDecimal;

public record RouteStopResponse(
    int waypointIndex, String slug, BigDecimal latitude, BigDecimal longitude, boolean optional) {}
