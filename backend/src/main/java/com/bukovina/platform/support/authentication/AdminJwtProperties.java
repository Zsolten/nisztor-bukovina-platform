package com.bukovina.platform.support.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "admin.authentication.jwt")
public record AdminJwtProperties(
    @NotBlank String issuer, @NotBlank String secret, @NotNull Duration accessTokenTtl) {}
