package com.bukovina.platform.accommodation.pricing.service;

import java.math.BigDecimal;
import java.util.List;

public record PricingView(
    String currency,
    List<Item> items,
    List<Adjustment> surcharges,
    List<Adjustment> discounts,
    String paymentNote) {

  public record Item(String id, String label, BigDecimal amount, String unit) {}

  public record Adjustment(String id, String label, BigDecimal percentage) {}
}
