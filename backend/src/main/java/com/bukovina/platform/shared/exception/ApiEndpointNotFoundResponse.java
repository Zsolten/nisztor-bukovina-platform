package com.bukovina.platform.shared.exception;

public record ApiEndpointNotFoundResponse(int status, String code, String path) {}
