package com.bukovina.platform.support.notification;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class ManagementTokenCipher {

  private static final int IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH_BITS = 128;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final SecretKey key;

  public ManagementTokenCipher(NotificationProperties properties) {
    this.key = properties.enabled() ? decodeKey(properties.tokenEncryptionKey()) : null;
  }

  public EncryptedManagementToken encrypt(String rawToken, String context) {
    requireEnabledKey();
    byte[] iv = new byte[IV_LENGTH];
    SECURE_RANDOM.nextBytes(iv);
    try {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
      cipher.updateAAD(context.getBytes(StandardCharsets.UTF_8));
      byte[] ciphertext = cipher.doFinal(rawToken.getBytes(StandardCharsets.UTF_8));
      return new EncryptedManagementToken(
          Base64.getEncoder().encodeToString(ciphertext), Base64.getEncoder().encodeToString(iv));
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("Could not encrypt management token", exception);
    }
  }

  public String decrypt(EncryptedManagementToken encryptedToken, String context) {
    requireEnabledKey();
    try {
      byte[] iv = Base64.getDecoder().decode(encryptedToken.initializationVector());
      byte[] ciphertext = Base64.getDecoder().decode(encryptedToken.ciphertext());
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
      cipher.updateAAD(context.getBytes(StandardCharsets.UTF_8));
      return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException | IllegalArgumentException exception) {
      throw new IllegalStateException("Could not decrypt management token", exception);
    }
  }

  private SecretKey decodeKey(String encodedKey) {
    try {
      byte[] bytes = Base64.getDecoder().decode(encodedKey == null ? "" : encodedKey);
      if (bytes.length != 32) {
        throw new IllegalStateException(
            "BOOKING_NOTIFICATION_TOKEN_ENCRYPTION_KEY must decode to exactly 32 bytes");
      }
      return new SecretKeySpec(bytes, "AES");
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException(
          "BOOKING_NOTIFICATION_TOKEN_ENCRYPTION_KEY must be Base64-encoded", exception);
    }
  }

  private void requireEnabledKey() {
    if (key == null) {
      throw new IllegalStateException("Notification token encryption is disabled");
    }
  }
}
