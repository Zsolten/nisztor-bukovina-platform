package com.bukovina.platform.accommodation.guesthouse.dto;

import java.util.UUID;

public record GuesthouseSummaryResponse(
    UUID id,
    String slug,
    String name,
    String shortDescription,
    int roomCount,
    GuesthouseImageResponse coverImage) {}
