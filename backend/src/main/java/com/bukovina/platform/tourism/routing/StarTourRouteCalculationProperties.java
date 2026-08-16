package com.bukovina.platform.tourism.routing;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tourism.routing.star-tour")
public record StarTourRouteCalculationProperties(
    @NotNull Duration failureCooldown, @NotNull Duration pendingTimeout) {}
