package com.bukovina.platform.accommodation.pricing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AdminPriceItemUpdateRequest(
    @NotBlank String code, @NotNull @DecimalMin("0.00") BigDecimal amount) {}
