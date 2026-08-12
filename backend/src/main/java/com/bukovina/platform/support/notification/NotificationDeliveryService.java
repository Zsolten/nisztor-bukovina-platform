package com.bukovina.platform.support.notification;

import jakarta.mail.MessagingException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDeliveryService {

  private static final String MAIL_SEND_FAILED = "MAIL_SEND_FAILED";
  private static final String NOTIFICATION_DATA_ERROR = "NOTIFICATION_DATA_ERROR";
  private static final String TOKEN_DECRYPTION_FAILED = "TOKEN_DECRYPTION_FAILED";

  private final NotificationOutboxRepository repository;
  private final NotificationBookingQueryDao bookingQueryDao;
  private final ManagementTokenCipher tokenCipher;
  private final BookingNotificationEmailFactory emailFactory;
  private final NotificationMailSender mailSender;
  private final NotificationProperties properties;

  public NotificationDeliveryService(
      NotificationOutboxRepository repository,
      NotificationBookingQueryDao bookingQueryDao,
      ManagementTokenCipher tokenCipher,
      BookingNotificationEmailFactory emailFactory,
      NotificationMailSender mailSender,
      NotificationProperties properties) {
    this.repository = repository;
    this.bookingQueryDao = bookingQueryDao;
    this.tokenCipher = tokenCipher;
    this.emailFactory = emailFactory;
    this.mailSender = mailSender;
    this.properties = properties;
  }

  @Transactional
  public void deliver(UUID jobId) {
    NotificationOutbox job = repository.findById(jobId).orElse(null);
    if (job == null || job.getStatus() != NotificationStatus.PROCESSING) {
      return;
    }

    try {
      NotificationBookingView booking =
          bookingQueryDao
              .findById(job.getBookingRequestId(), job.getLanguageCode())
              .orElseThrow(NotificationDataException::new);
      NotificationEmailContent content = content(job, booking);
      mailSender.send(job.getRecipient(), job.getReplyTo(), content);
      job.markDelivered(Instant.now());
    } catch (TokenDecryptionException exception) {
      markFailed(job, TOKEN_DECRYPTION_FAILED);
    } catch (MessagingException | MailException exception) {
      markFailed(job, MAIL_SEND_FAILED);
    } catch (RuntimeException exception) {
      markFailed(job, NOTIFICATION_DATA_ERROR);
    }
    repository.flush();
  }

  private NotificationEmailContent content(
      NotificationOutbox job, NotificationBookingView booking) {
    if (job.getNotificationType() == NotificationType.BOOKING_RECEIVED_ADMIN) {
      return emailFactory.admin(booking);
    }
    EncryptedManagementToken encryptedToken = job.getEncryptedManagementToken();
    if (encryptedToken == null) {
      throw new TokenDecryptionException();
    }
    try {
      String rawToken = tokenCipher.decrypt(encryptedToken, job.getBookingRequestId().toString());
      return emailFactory.guest(booking, job.getLanguageCode(), rawToken);
    } catch (IllegalStateException exception) {
      throw new TokenDecryptionException();
    }
  }

  private void markFailed(NotificationOutbox job, String errorCode) {
    job.markFailed(
        Instant.now(), properties.maxAttempts(), properties.retryInitialDelaySeconds(), errorCode);
  }

  private static final class NotificationDataException extends RuntimeException {}

  private static final class TokenDecryptionException extends RuntimeException {}
}
