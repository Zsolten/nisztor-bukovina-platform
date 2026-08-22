package com.bukovina.platform.tourism.startour.model;

public record DrivingMatrixLeg(
    Integer distanceMeters, Integer durationSeconds, String status, String failureReason) {}
