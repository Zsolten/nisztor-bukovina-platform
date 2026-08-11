package com.bukovina.platform.support.authentication;

import com.bukovina.platform.support.authentication.dto.AdminLoginRequest;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class AdminLoginRateLimiter {

  private static final String UNKNOWN_CLIENT = "unknown";
  private static final String INVALID_EMAIL = "invalid-email";

  private final AdminLoginRateLimitProperties properties;
  private final Cache<LoginAttemptKey, Bucket> buckets;

  public AdminLoginRateLimiter(AdminLoginRateLimitProperties properties) {
    this.properties = properties;
    buckets =
        Caffeine.newBuilder()
            .maximumSize(properties.maxEntries())
            .expireAfterAccess(properties.cacheTtl())
            .build();
  }

  public void consume(String clientIp, AdminLoginRequest request) {
    if (!properties.enabled()) {
      return;
    }
    Bucket bucket = buckets.get(keyFor(clientIp, request), ignored -> newBucket());
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
    if (!probe.isConsumed()) {
      throw new AdminLoginRateLimitException(secondsUntil(probe.getNanosToWaitForRefill()));
    }
  }

  public void reset(String clientIp, AdminLoginRequest request) {
    if (properties.enabled()) {
      buckets.invalidate(keyFor(clientIp, request));
    }
  }

  private Bucket newBucket() {
    return Bucket.builder()
        .addLimit(
            limit ->
                limit
                    .capacity(properties.capacity())
                    .refillGreedy(properties.capacity(), properties.refillPeriod()))
        .build();
  }

  private LoginAttemptKey keyFor(String clientIp, AdminLoginRequest request) {
    String email = request == null ? null : request.email();
    return new LoginAttemptKey(normalizedClientIp(clientIp), normalizedEmail(email));
  }

  private String normalizedClientIp(String value) {
    return value == null || value.isBlank() ? UNKNOWN_CLIENT : value;
  }

  private String normalizedEmail(String value) {
    if (value == null) {
      return INVALID_EMAIL;
    }
    String normalized = value.strip().toLowerCase(Locale.ROOT);
    return normalized.isEmpty() || normalized.length() > 320 ? INVALID_EMAIL : normalized;
  }

  private long secondsUntil(long nanos) {
    long nanosPerSecond = Duration.ofSeconds(1).toNanos();
    return Math.max(1, (nanos + nanosPerSecond - 1) / nanosPerSecond);
  }

  private record LoginAttemptKey(String clientIp, String email) {}
}
