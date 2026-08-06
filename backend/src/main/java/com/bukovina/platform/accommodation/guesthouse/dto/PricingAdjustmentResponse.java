package com.bukovina.platform.accommodation.guesthouse.dto;

import java.math.BigDecimal;

public record PricingAdjustmentResponse(String id, String label, BigDecimal percentage) {}
