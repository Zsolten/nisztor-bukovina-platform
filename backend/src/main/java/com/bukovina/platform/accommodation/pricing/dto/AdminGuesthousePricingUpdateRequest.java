package com.bukovina.platform.accommodation.pricing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AdminGuesthousePricingUpdateRequest(
    @NotNull List<@Valid AdminPriceItemUpdateRequest> items,
    @NotNull List<@Valid AdminPricingAdjustmentUpdateRequest> surcharges,
    @NotNull List<@Valid AdminPricingAdjustmentUpdateRequest> discounts) {}
