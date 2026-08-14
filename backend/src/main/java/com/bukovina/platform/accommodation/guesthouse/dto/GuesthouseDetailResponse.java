package com.bukovina.platform.accommodation.guesthouse.dto;

import java.util.List;
import java.util.UUID;

public record GuesthouseDetailResponse(
    UUID id,
    String slug,
    String name,
    String shortDescription,
    int roomCount,
    GuesthouseImageResponse coverImage,
    String description,
    String roomDescription,
    GuesthousePageTextResponse pageText,
    List<GuesthouseImageResponse> images,
    GuesthouseHistoryResponse history,
    List<GuesthouseContactResponse> contacts,
    GuesthouseAddressResponse address,
    List<RoomTypeResponse> roomTypes,
    List<AmenityResponse> amenities,
    GuesthousePricingResponse pricing) {}
