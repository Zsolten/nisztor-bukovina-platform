package com.bukovina.platform.tourism.startour.model;

import java.util.UUID;

public record PublicStarTour(
    UUID id,
    String slug,
    String name,
    String shortDescription,
    String detailedDescription,
    String mapColor) {}
