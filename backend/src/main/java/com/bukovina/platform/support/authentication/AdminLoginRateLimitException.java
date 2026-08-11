package com.bukovina.platform.support.authentication;

public class AdminLoginRateLimitException extends RuntimeException {

  private final long retryAfterSeconds;

  public AdminLoginRateLimitException(long retryAfterSeconds) {
    super("Too many administrator login attempts");
    this.retryAfterSeconds = retryAfterSeconds;
  }

  public long getRetryAfterSeconds() {
    return retryAfterSeconds;
  }
}
