package com.bukovina.platform.tourism.startour.model;

import java.util.List;

public record RouteDefinition(List<RouteTourStop> stops, String selectionKey, String fingerprint) {}
