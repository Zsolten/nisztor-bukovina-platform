package com.bukovina.platform.tourism.startour.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RouteVariant(
    UUID id,
    String status,
    OffsetDateTime calculatedAt,
    String failureReason,
    OffsetDateTime retryAfter) {}
