package com.bukovina.platform.accommodation.booking.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** Limits anonymous booking requests before they can create database or email work. */
@Service
public class PublicBookingRateLimiter {

  private static final String UNKNOWN_CLIENT = "unknown";
  private static final String INVALID_EMAIL = "invalid-email";

  private final PublicBookingRateLimitProperties properties;
  private final Cache<String, Bucket> quoteClientBuckets;
  private final Cache<String, Bucket> requestClientBuckets;
  private final Cache<String, Bucket> requestEmailBuckets;

  public PublicBookingRateLimiter(PublicBookingRateLimitProperties properties) {
    this.properties = properties;
    quoteClientBuckets = newCache(properties);
    requestClientBuckets = newCache(properties);
    requestEmailBuckets = newCache(properties);
  }

  public void consumeQuote(String clientIp) {
    if (properties.enabled()) {
      consume(
          quoteClientBuckets,
          normalizedClientIp(clientIp),
          properties.quoteClientCapacity(),
          properties.quoteClientRefillPeriod());
    }
  }

  public void consumeRequest(String clientIp, String email) {
    if (!properties.enabled()) {
      return;
    }
    consume(
        requestClientBuckets,
        normalizedClientIp(clientIp),
        properties.requestClientCapacity(),
        properties.requestClientRefillPeriod());
    consume(
        requestEmailBuckets,
        normalizedEmail(email),
        properties.requestEmailCapacity(),
        properties.requestEmailRefillPeriod());
  }

  private static Cache<String, Bucket> newCache(PublicBookingRateLimitProperties properties) {
    return Caffeine.newBuilder()
        .maximumSize(properties.maxEntries())
        .expireAfterAccess(properties.cacheTtl())
        .build();
  }

  private void consume(
      Cache<String, Bucket> buckets, String key, int capacity, Duration refillPeriod) {
    ConsumptionProbe probe =
        buckets
            .get(key, ignored -> newBucket(capacity, refillPeriod))
            .tryConsumeAndReturnRemaining(1);
    if (!probe.isConsumed()) {
      throw new PublicBookingRateLimitException(secondsUntil(probe.getNanosToWaitForRefill()));
    }
  }

  private static Bucket newBucket(int capacity, Duration refillPeriod) {
    return Bucket.builder()
        .addLimit(limit -> limit.capacity(capacity).refillGreedy(capacity, refillPeriod))
        .build();
  }

  private static String normalizedClientIp(String value) {
    return value == null || value.isBlank() ? UNKNOWN_CLIENT : value;
  }

  private static String normalizedEmail(String value) {
    if (value == null) {
      return INVALID_EMAIL;
    }
    String normalized = value.strip().toLowerCase(Locale.ROOT);
    return normalized.isBlank() || normalized.length() > 320 ? INVALID_EMAIL : normalized;
  }

  private static long secondsUntil(long nanos) {
    long nanosPerSecond = Duration.ofSeconds(1).toNanos();
    return Math.max(1, (nanos + nanosPerSecond - 1) / nanosPerSecond);
  }
}
