package com.bukovina.platform.support.notification;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "booking.notification")
public record NotificationProperties(
    boolean enabled,
    int maxAttempts,
    long retryInitialDelaySeconds,
    Duration workerDelay,
    String tokenEncryptionKey,
    String publicBaseUrl,
    String adminBaseUrl,
    String fromAddress,
    String fromName) {

  public NotificationProperties {
    if (maxAttempts < 1) {
      throw new IllegalArgumentException("booking.notification.max-attempts must be positive");
    }
    if (retryInitialDelaySeconds < 1) {
      throw new IllegalArgumentException(
          "booking.notification.retry-initial-delay-seconds must be positive");
    }
    if (workerDelay == null || workerDelay.isZero() || workerDelay.isNegative()) {
      throw new IllegalArgumentException("booking.notification.worker-delay must be positive");
    }
    if (enabled) {
      requireConfigured(publicBaseUrl, "booking.notification.public-base-url");
      requireConfigured(adminBaseUrl, "booking.notification.admin-base-url");
      requireConfigured(fromAddress, "booking.notification.from-address");
      requireConfigured(fromName, "booking.notification.from-name");
    }
  }

  private static void requireConfigured(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(
          name + " must be configured when notifications are enabled");
    }
  }
}
