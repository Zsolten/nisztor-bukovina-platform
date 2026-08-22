package com.bukovina.platform.tourism.activity.dto;

public record AttractionTranslation(
    String language,
    String name,
    String shortDescription,
    String detailedDescription,
    String admissionInformation,
    String practicalInformation) {}
