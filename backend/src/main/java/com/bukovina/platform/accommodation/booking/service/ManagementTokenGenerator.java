package com.bukovina.platform.accommodation.booking.service;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class ManagementTokenGenerator {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  public GeneratedManagementToken generate() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    return new GeneratedManagementToken(rawToken, BookingHashing.sha256(rawToken));
  }
}
