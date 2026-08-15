package com.bukovina.platform.tourism.routing;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GoogleRoutesProperties.class)
class RoutingConfiguration {}
