package com.bukovina.platform.accommodation.roomtype.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminRoomTypeTranslationUpdateRequest(
    @NotBlank @Size(max = 2) String language,
    @NotNull @Size(max = 160) String name,
    @NotNull @Size(max = 1000) String shortDescription,
    @NotNull @Size(max = 5000) String detailedDescription) {}
