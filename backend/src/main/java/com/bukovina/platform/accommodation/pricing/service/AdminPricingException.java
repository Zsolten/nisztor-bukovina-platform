package com.bukovina.platform.accommodation.pricing.service;

public class AdminPricingException extends RuntimeException {

  private final String code;

  public AdminPricingException(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
