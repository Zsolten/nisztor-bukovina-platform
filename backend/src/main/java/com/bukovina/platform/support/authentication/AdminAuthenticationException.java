package com.bukovina.platform.support.authentication;

public class AdminAuthenticationException extends RuntimeException {

  public AdminAuthenticationException() {
    super("Invalid administrator credentials");
  }
}
