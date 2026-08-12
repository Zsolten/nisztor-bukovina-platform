package com.bukovina.platform.support.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {

  @Id private UUID id;

  @Column(name = "booking_request_id", nullable = false)
  private UUID bookingRequestId;

  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type", nullable = false, length = 40)
  private NotificationType notificationType;

  @Column(nullable = false, length = 320)
  private String recipient;

  @Column(name = "reply_to", length = 320)
  private String replyTo;

  @Column(name = "language_code", nullable = false, length = 2)
  private String languageCode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationStatus status;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Column(name = "next_attempt_at", nullable = false)
  private Instant nextAttemptAt;

  @Column(name = "last_attempt_at")
  private Instant lastAttemptAt;

  @Column(name = "delivered_at")
  private Instant deliveredAt;

  @Column(name = "last_error_code", length = 100)
  private String lastErrorCode;

  @Column(name = "encrypted_management_token")
  private String encryptedManagementToken;

  @Column(name = "token_initialization_vector", length = 64)
  private String tokenInitializationVector;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected NotificationOutbox() {}

  private NotificationOutbox(
      UUID bookingRequestId,
      NotificationType notificationType,
      String recipient,
      String replyTo,
      String languageCode,
      EncryptedManagementToken encryptedToken,
      Instant now) {
    this.id = UUID.randomUUID();
    this.bookingRequestId = Objects.requireNonNull(bookingRequestId);
    this.notificationType = Objects.requireNonNull(notificationType);
    this.recipient = Objects.requireNonNull(recipient);
    this.replyTo = replyTo;
    this.languageCode = Objects.requireNonNull(languageCode);
    this.status = NotificationStatus.PENDING;
    this.nextAttemptAt = now;
    this.createdAt = now;
    this.updatedAt = now;
    if (encryptedToken != null) {
      this.encryptedManagementToken = encryptedToken.ciphertext();
      this.tokenInitializationVector = encryptedToken.initializationVector();
    }
  }

  public static NotificationOutbox guest(
      UUID bookingRequestId,
      String recipient,
      String replyTo,
      String languageCode,
      EncryptedManagementToken encryptedToken,
      Instant now) {
    return new NotificationOutbox(
        bookingRequestId,
        NotificationType.BOOKING_RECEIVED_GUEST,
        recipient,
        replyTo,
        languageCode,
        encryptedToken,
        now);
  }

  public static NotificationOutbox admin(UUID bookingRequestId, String recipient, Instant now) {
    return new NotificationOutbox(
        bookingRequestId,
        NotificationType.BOOKING_RECEIVED_ADMIN,
        recipient,
        null,
        "hu",
        null,
        now);
  }

  public void markProcessing(Instant now) {
    status = NotificationStatus.PROCESSING;
    attemptCount++;
    lastAttemptAt = now;
    updatedAt = now;
    lastErrorCode = null;
  }

  public void markDelivered(Instant now) {
    status = NotificationStatus.DELIVERED;
    deliveredAt = now;
    updatedAt = now;
    lastErrorCode = null;
    encryptedManagementToken = null;
    tokenInitializationVector = null;
  }

  public void markFailed(Instant now, int maxAttempts, long initialDelaySeconds, String errorCode) {
    updatedAt = now;
    lastErrorCode = errorCode;
    if (attemptCount >= maxAttempts) {
      status = NotificationStatus.EXHAUSTED;
      encryptedManagementToken = null;
      tokenInitializationVector = null;
      return;
    }
    status = NotificationStatus.RETRY;
    long multiplier = 1L << Math.max(0, attemptCount - 1);
    nextAttemptAt = now.plusSeconds(Math.multiplyExact(initialDelaySeconds, multiplier));
  }

  public UUID getId() {
    return id;
  }

  public UUID getBookingRequestId() {
    return bookingRequestId;
  }

  public NotificationType getNotificationType() {
    return notificationType;
  }

  public String getRecipient() {
    return recipient;
  }

  public String getReplyTo() {
    return replyTo;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  public NotificationStatus getStatus() {
    return status;
  }

  public int getAttemptCount() {
    return attemptCount;
  }

  public String getLastErrorCode() {
    return lastErrorCode;
  }

  public EncryptedManagementToken getEncryptedManagementToken() {
    if (encryptedManagementToken == null || tokenInitializationVector == null) {
      return null;
    }
    return new EncryptedManagementToken(encryptedManagementToken, tokenInitializationVector);
  }
}
