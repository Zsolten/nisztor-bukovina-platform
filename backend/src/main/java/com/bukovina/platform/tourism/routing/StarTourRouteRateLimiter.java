package com.bukovina.platform.tourism.routing;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StarTourRouteRateLimiter {

  private static final String UNKNOWN_CLIENT = "unknown";
  private final StarTourRouteRateLimitProperties properties;
  private final Bucket globalBucket;
  private final Cache<String, Bucket> clientBuckets;

  public StarTourRouteRateLimiter(StarTourRouteRateLimitProperties properties) {
    this.properties = properties;
    globalBucket = newBucket(properties.globalCapacity(), properties.globalRefillPeriod());
    clientBuckets =
        Caffeine.newBuilder()
            .maximumSize(properties.maxEntries())
            .expireAfterAccess(properties.cacheTtl())
            .build();
  }

  /** Consumes a token only when a request is about to calculate an uncached route. */
  public void consumeCalculation(String clientIp) {
    if (!properties.enabled()) {
      return;
    }
    ConsumptionProbe client =
        clientBuckets
            .get(
                normalize(clientIp),
                ignored -> newBucket(properties.clientCapacity(), properties.clientRefillPeriod()))
            .tryConsumeAndReturnRemaining(1);
    if (!client.isConsumed()) {
      throw limited(client.getNanosToWaitForRefill());
    }
    ConsumptionProbe global = globalBucket.tryConsumeAndReturnRemaining(1);
    if (!global.isConsumed()) {
      throw limited(global.getNanosToWaitForRefill());
    }
  }

  private static Bucket newBucket(int capacity, Duration refillPeriod) {
    return Bucket.builder()
        .addLimit(limit -> limit.capacity(capacity).refillGreedy(capacity, refillPeriod))
        .build();
  }

  private static String normalize(String clientIp) {
    return clientIp == null || clientIp.isBlank() ? UNKNOWN_CLIENT : clientIp;
  }

  private static ResponseStatusException limited(long nanosToWait) {
    long seconds =
        Math.max(
            1,
            (nanosToWait + Duration.ofSeconds(1).toNanos() - 1) / Duration.ofSeconds(1).toNanos());
    return new ResponseStatusException(
        HttpStatus.TOO_MANY_REQUESTS, "STAR_TOUR_ROUTE_RATE_LIMITED_" + seconds);
  }
}
