package com.bukovina.platform.accommodation.guesthouse.dto;

public record AdminGuesthouseTranslationResponse(
    String language,
    Long version,
    String name,
    String shortDescription,
    String description,
    String roomDescription,
    String storyEyebrow,
    String storyTitle,
    String diningEyebrow,
    String diningTitle,
    String diningDescription,
    String amenitiesTitle,
    String roomTypesTitle,
    String pricingTitle,
    String historyEyebrow,
    String historyTitle,
    String historyText,
    String galleryTitle,
    String galleryHint) {}
