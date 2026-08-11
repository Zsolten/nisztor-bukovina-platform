package com.bukovina.platform.support.authentication.dto;

import java.time.Instant;

public record AdminLoginResponse(String accessToken, String tokenType, Instant expiresAt) {}
