package com.bukovina.platform.accommodation.pricing.dto;

import java.util.List;
import java.util.UUID;

public record AdminGuesthousePricingResponse(
    UUID guesthouseId,
    String currency,
    List<AdminPriceItemResponse> items,
    List<AdminPricingAdjustmentResponse> surcharges,
    List<AdminPricingAdjustmentResponse> discounts) {}
