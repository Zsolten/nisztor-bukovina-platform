package com.bukovina.platform.support.authentication;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "admin.authentication.login-rate-limit")
public record AdminLoginRateLimitProperties(
    boolean enabled,
    @Positive int capacity,
    @NotNull Duration refillPeriod,
    @NotNull Duration cacheTtl,
    @Positive long maxEntries) {}
