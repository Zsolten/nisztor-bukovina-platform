package com.bukovina.platform.tourism.startour.model;

import java.util.UUID;

public record StarTour(UUID id, String slug, String mapColor, boolean published, boolean active) {}
