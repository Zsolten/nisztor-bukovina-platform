package com.bukovina.platform.support.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.authentication.bootstrap")
public record AdminBootstrapProperties(String email, String password) {}
