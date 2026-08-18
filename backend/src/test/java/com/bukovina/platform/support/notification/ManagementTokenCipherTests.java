package com.bukovina.platform.support.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class ManagementTokenCipherTests {

  private static final String KEY = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";
  private final ManagementTokenCipher cipher = new ManagementTokenCipher(properties());

  @Test
  void encryptsWithAUniqueInitializationVectorAndBookingBoundAuthentication() {
    EncryptedManagementToken first = cipher.encrypt("raw-secret", "booking-one");
    EncryptedManagementToken second = cipher.encrypt("raw-secret", "booking-one");

    assertNotEquals(first, second);
    assertEquals("raw-secret", cipher.decrypt(first, "booking-one"));
    assertThrows(IllegalStateException.class, () -> cipher.decrypt(first, "booking-two"));
  }

  private NotificationProperties properties() {
    return new NotificationProperties(
        true,
        5,
        60,
        Duration.ofSeconds(10),
        KEY,
        "http://localhost:5173",
        "http://localhost:5173",
        "sender@example.com",
        "Sender");
  }
}
