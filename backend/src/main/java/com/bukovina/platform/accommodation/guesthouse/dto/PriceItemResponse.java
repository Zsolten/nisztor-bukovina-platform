package com.bukovina.platform.accommodation.guesthouse.dto;

import java.math.BigDecimal;

public record PriceItemResponse(String id, String label, BigDecimal amount, String unit) {}
