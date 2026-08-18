package com.bukovina.platform.accommodation.guesthouse.dto;

import java.util.Map;

public record AdminGuesthouseContentErrorResponse(
    String code,
    Map<String, String> fieldErrors,
    AdminGuesthouseTranslationResponse currentContent) {}
