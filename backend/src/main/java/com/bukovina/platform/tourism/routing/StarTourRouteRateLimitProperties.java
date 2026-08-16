package com.bukovina.platform.tourism.routing;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tourism.routing.star-tour.rate-limit")
public record StarTourRouteRateLimitProperties(
    boolean enabled,
    @Positive int globalCapacity,
    @NotNull Duration globalRefillPeriod,
    @Positive int clientCapacity,
    @NotNull Duration clientRefillPeriod,
    @NotNull Duration cacheTtl,
    @Positive long maxEntries) {}
