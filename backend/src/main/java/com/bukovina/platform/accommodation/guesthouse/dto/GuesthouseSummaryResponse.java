package com.bukovina.platform.accommodation.guesthouse.dto;

public record GuesthouseSummaryResponse(
    String slug,
    String name,
    String shortDescription,
    int roomCount,
    GuesthouseImageResponse coverImage) {}
