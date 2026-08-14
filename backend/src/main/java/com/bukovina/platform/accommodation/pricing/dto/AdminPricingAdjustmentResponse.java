package com.bukovina.platform.accommodation.pricing.dto;

import java.math.BigDecimal;

public record AdminPricingAdjustmentResponse(String code, String label, BigDecimal percentage) {}
