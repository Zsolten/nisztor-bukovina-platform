package com.bukovina.platform.accommodation.amenity.dto;

import java.util.Map;

public record AdminAmenityErrorResponse(String code, Map<String, String> fieldErrors) {}
