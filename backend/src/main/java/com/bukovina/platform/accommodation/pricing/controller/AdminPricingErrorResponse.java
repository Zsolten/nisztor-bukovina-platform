package com.bukovina.platform.accommodation.pricing.controller;

import java.util.Map;

public record AdminPricingErrorResponse(String code, Map<String, String> fieldErrors) {}
