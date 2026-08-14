package com.bukovina.platform.accommodation.guesthouse.dto;

import java.util.List;
import java.util.UUID;

public record AdminGuesthouseContentResponse(
    UUID id, String slug, boolean active, List<AdminGuesthouseTranslationResponse> translations) {}
