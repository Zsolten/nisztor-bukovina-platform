package com.bukovina.platform.support.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;

class NotificationDeliveryServiceTests {

  private static final String KEY = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

  @Test
  void decryptsTheGuestLinkOnlyForDeliveryAndClearsTheCiphertextAfterSuccess() throws Exception {
    NotificationProperties properties = properties(5);
    ManagementTokenCipher cipher = new ManagementTokenCipher(properties);
    UUID bookingId = UUID.randomUUID();
    NotificationOutbox job =
        NotificationOutbox.guest(
            bookingId,
            "guest@example.com",
            "guesthouse@example.com",
            "hu",
            cipher.encrypt("raw-management-token", bookingId.toString()),
            Instant.now());
    job.markProcessing(Instant.now());
    NotificationOutboxRepository repository = mock(NotificationOutboxRepository.class);
    NotificationBookingQueryDao queryDao = mock(NotificationBookingQueryDao.class);
    NotificationMailSender mailSender = mock(NotificationMailSender.class);
    when(repository.findById(job.getId())).thenReturn(Optional.of(job));
    when(queryDao.findById(bookingId, "hu")).thenReturn(Optional.of(booking(bookingId)));
    NotificationDeliveryService service =
        new NotificationDeliveryService(
            repository,
            queryDao,
            cipher,
            new BookingNotificationEmailFactory(properties),
            mailSender,
            properties);

    service.deliver(job.getId());

    ArgumentCaptor<NotificationEmailContent> content =
        ArgumentCaptor.forClass(NotificationEmailContent.class);
    verify(mailSender)
        .send(eq("guest@example.com"), eq("guesthouse@example.com"), content.capture());
    assertTrue(content.getValue().body().contains("/hu/booking-management/raw-management-token"));
    assertEquals(NotificationStatus.DELIVERED, job.getStatus());
    assertNull(job.getEncryptedManagementToken());
  }

  @Test
  void schedulesABoundedRetryWhenSmtpDeliveryFails() throws Exception {
    NotificationProperties properties = properties(2);
    UUID bookingId = UUID.randomUUID();
    NotificationOutbox job =
        NotificationOutbox.admin(bookingId, "admin@example.com", Instant.now());
    job.markProcessing(Instant.now());
    NotificationOutboxRepository repository = mock(NotificationOutboxRepository.class);
    NotificationBookingQueryDao queryDao = mock(NotificationBookingQueryDao.class);
    NotificationMailSender mailSender = mock(NotificationMailSender.class);
    when(repository.findById(job.getId())).thenReturn(Optional.of(job));
    when(queryDao.findById(bookingId, "hu")).thenReturn(Optional.of(booking(bookingId)));
    doThrow(new MailSendException("SMTP unavailable")).when(mailSender).send(any(), any(), any());
    NotificationDeliveryService service =
        new NotificationDeliveryService(
            repository,
            queryDao,
            new ManagementTokenCipher(properties),
            new BookingNotificationEmailFactory(properties),
            mailSender,
            properties);

    service.deliver(job.getId());

    assertEquals(NotificationStatus.RETRY, job.getStatus());
    assertEquals(1, job.getAttemptCount());
    assertEquals("MAIL_SEND_FAILED", job.getLastErrorCode());
    verify(repository).flush();
  }

  private NotificationProperties properties(int maxAttempts) {
    return new NotificationProperties(
        true,
        maxAttempts,
        60,
        Duration.ofSeconds(10),
        KEY,
        "https://example.com",
        "https://example.com",
        "sender@example.com",
        "Sender");
  }

  private NotificationBookingView booking(UUID bookingId) {
    return new NotificationBookingView(
        bookingId,
        "NB-1234567890ABCDEF",
        "Bukovina Panzió",
        LocalDate.of(2026, 9, 1),
        LocalDate.of(2026, 9, 3),
        2,
        2,
        0,
        0,
        2,
        0,
        new BigDecimal("520.00"),
        BigDecimal.ZERO,
        new BigDecimal("180.00"),
        BigDecimal.ZERO,
        new BigDecimal("700.00"),
        "RON",
        List.of(new NotificationRoomView("Kétágyas szoba", 1)));
  }
}
