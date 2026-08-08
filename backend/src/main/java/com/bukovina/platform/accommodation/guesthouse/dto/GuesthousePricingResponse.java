package com.bukovina.platform.accommodation.guesthouse.dto;

import java.util.List;

public record GuesthousePricingResponse(
    String currency,
    List<PriceItemResponse> items,
    List<PricingAdjustmentResponse> taxes,
    List<PricingAdjustmentResponse> surcharges,
    List<PricingAdjustmentResponse> discounts,
    String paymentNote) {}
