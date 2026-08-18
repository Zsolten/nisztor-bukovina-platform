package com.bukovina.platform.accommodation.pricing.dto;

import java.math.BigDecimal;

public record AdminPriceItemResponse(String code, String label, BigDecimal amount, String unit) {}
