package com.bukovina.platform.accommodation.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ManagementTokenGeneratorTests {

  private final ManagementTokenGenerator generator = new ManagementTokenGenerator();

  @Test
  void returnsAUrlSafeRandomTokenAndStoresOnlyItsHash() {
    GeneratedManagementToken first = generator.generate();
    GeneratedManagementToken second = generator.generate();

    assertTrue(first.rawToken().matches("[A-Za-z0-9_-]{43}"));
    assertEquals(BookingHashing.sha256(first.rawToken()), first.tokenHash());
    assertNotEquals(first.rawToken(), first.tokenHash());
    assertNotEquals(first.rawToken(), second.rawToken());
  }
}
