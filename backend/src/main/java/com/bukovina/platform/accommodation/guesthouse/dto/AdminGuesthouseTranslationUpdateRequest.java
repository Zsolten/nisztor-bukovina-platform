package com.bukovina.platform.accommodation.guesthouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminGuesthouseTranslationUpdateRequest(
    Long version,
    @NotBlank @Size(max = 160) String name,
    @NotBlank @Size(max = 500) String shortDescription,
    @NotBlank @Size(max = 5000) String description,
    @NotBlank @Size(max = 3000) String roomDescription,
    @NotBlank @Size(max = 240) String historyTitle,
    @NotBlank @Size(max = 5000) String historyText) {}
