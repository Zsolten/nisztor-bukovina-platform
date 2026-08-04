package com.bukovina.platform.accommodation.guesthouse.dto;

import java.util.List;

public record GuesthouseDetailResponse(
    String slug,
    String name,
    String shortDescription,
    int roomCount,
    GuesthouseImageResponse coverImage,
    String description,
    String roomDescription,
    List<GuesthouseImageResponse> images) {}
