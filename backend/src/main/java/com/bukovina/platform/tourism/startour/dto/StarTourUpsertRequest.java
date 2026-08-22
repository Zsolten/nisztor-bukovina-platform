package com.bukovina.platform.tourism.startour.dto;

import java.util.List;

public record StarTourUpsertRequest(
    String slug,
    String mapColor,
    Boolean published,
    Boolean active,
    List<StarTourTranslation> translations,
    List<String> tags,
    List<StarTourImage> images) {}
