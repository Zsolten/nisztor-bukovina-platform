package com.bukovina.platform.accommodation.guesthouse.service;

public class AdminGuesthouseContentValidationException extends RuntimeException {

  private final String code;

  public AdminGuesthouseContentValidationException(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
