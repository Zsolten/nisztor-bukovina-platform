package com.bukovina.platform.shared.configuration;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;

@Configuration
public class JacksonConfiguration {

  @Bean
  JsonMapperBuilderCustomizer rejectUnknownJsonProperties() {
    return builder -> {
      builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      builder.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
    };
  }
}
