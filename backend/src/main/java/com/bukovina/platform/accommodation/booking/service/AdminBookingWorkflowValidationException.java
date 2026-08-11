package com.bukovina.platform.accommodation.booking.service;

public class AdminBookingWorkflowValidationException extends RuntimeException {

  private final String code;

  public AdminBookingWorkflowValidationException(String code) {
    super(code);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
