package com.bukovina.platform.accommodation.amenity.service;

public class AdminAmenityException extends RuntimeException {

  private final String code;

  public AdminAmenityException(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
