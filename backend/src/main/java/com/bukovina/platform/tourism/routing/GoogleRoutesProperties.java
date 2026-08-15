package com.bukovina.platform.tourism.routing;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tourism.routing.google")
public record GoogleRoutesProperties(String apiKey, String baseUrl) {

  public GoogleRoutesProperties {
    baseUrl =
        baseUrl == null || baseUrl.isBlank()
            ? "https://routes.googleapis.com"
            : baseUrl.replaceAll("/+$", "");
  }
}
