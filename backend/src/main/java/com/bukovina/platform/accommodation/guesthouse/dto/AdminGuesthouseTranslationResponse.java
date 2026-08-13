package com.bukovina.platform.accommodation.guesthouse.dto;

public record AdminGuesthouseTranslationResponse(
    String language,
    Long version,
    String name,
    String shortDescription,
    String description,
    String roomDescription,
    String historyTitle,
    String historyText) {}
