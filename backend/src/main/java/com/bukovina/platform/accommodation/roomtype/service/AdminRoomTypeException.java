package com.bukovina.platform.accommodation.roomtype.service;

public class AdminRoomTypeException extends RuntimeException {

  private final String code;

  public AdminRoomTypeException(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
