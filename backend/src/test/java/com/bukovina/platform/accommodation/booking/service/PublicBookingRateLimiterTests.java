package com.bukovina.platform.accommodation.booking.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class PublicBookingRateLimiterTests {

  @Test
  void limitsBookingRequestsByNormalizedEmailAcrossDifferentClients() {
    PublicBookingRateLimiter limiter = new PublicBookingRateLimiter(properties());

    limiter.consumeRequest("198.51.100.10", "Guest@Example.com");

    assertThrows(
        PublicBookingRateLimitException.class,
        () -> limiter.consumeRequest("198.51.100.11", "guest@example.com"));
  }

  private PublicBookingRateLimitProperties properties() {
    Duration oneDay = Duration.ofDays(1);
    return new PublicBookingRateLimitProperties(true, 1, oneDay, 1, oneDay, 1, oneDay, oneDay, 100);
  }
}
