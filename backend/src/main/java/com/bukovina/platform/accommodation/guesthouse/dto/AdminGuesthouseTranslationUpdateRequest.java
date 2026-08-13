package com.bukovina.platform.accommodation.guesthouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminGuesthouseTranslationUpdateRequest(
    Long version,
    @NotBlank @Size(max = 160) String name,
    @NotBlank @Size(max = 500) String shortDescription,
    @NotBlank @Size(max = 5000) String description,
    @NotBlank @Size(max = 3000) String roomDescription,
    @NotBlank @Size(max = 240) String storyEyebrow,
    @NotBlank @Size(max = 240) String storyTitle,
    @NotBlank @Size(max = 240) String diningEyebrow,
    @NotBlank @Size(max = 240) String diningTitle,
    @NotBlank @Size(max = 1000) String diningDescription,
    @NotBlank @Size(max = 240) String amenitiesTitle,
    @NotBlank @Size(max = 240) String roomTypesTitle,
    @NotBlank @Size(max = 240) String pricingTitle,
    @NotBlank @Size(max = 240) String historyEyebrow,
    @NotBlank @Size(max = 240) String historyTitle,
    @NotBlank @Size(max = 5000) String historyText,
    @NotBlank @Size(max = 240) String galleryTitle,
    @NotBlank @Size(max = 500) String galleryHint) {}
