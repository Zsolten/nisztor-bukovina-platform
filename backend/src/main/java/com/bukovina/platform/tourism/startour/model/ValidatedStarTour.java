package com.bukovina.platform.tourism.startour.model;

import java.util.List;
import java.util.Map;

public record ValidatedStarTour(
    String slug,
    String mapColor,
    boolean published,
    boolean active,
    Map<String, StarTourContent> translations,
    List<String> tags,
    List<StarTourImageData> images) {}
