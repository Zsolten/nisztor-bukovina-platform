package com.bukovina.platform.accommodation.booking.service;

public class PublicBookingRateLimitException extends RuntimeException {

  private final long retryAfterSeconds;

  public PublicBookingRateLimitException(long retryAfterSeconds) {
    this.retryAfterSeconds = retryAfterSeconds;
  }

  public long getRetryAfterSeconds() {
    return retryAfterSeconds;
  }
}
