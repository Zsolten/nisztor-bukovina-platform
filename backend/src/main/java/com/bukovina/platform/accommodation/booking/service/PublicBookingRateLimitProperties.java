package com.bukovina.platform.accommodation.booking.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "booking.public.rate-limit")
public record PublicBookingRateLimitProperties(
    boolean enabled,
    @Positive int quoteClientCapacity,
    @NotNull Duration quoteClientRefillPeriod,
    @Positive int requestClientCapacity,
    @NotNull Duration requestClientRefillPeriod,
    @Positive int requestEmailCapacity,
    @NotNull Duration requestEmailRefillPeriod,
    @NotNull Duration cacheTtl,
    @Positive long maxEntries) {}
