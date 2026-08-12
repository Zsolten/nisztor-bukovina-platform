package com.bukovina.platform.support.notification;

import com.bukovina.platform.accommodation.booking.model.BookingRequest;
import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import com.bukovina.platform.accommodation.booking.service.BookingNotificationOutbox;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class DatabaseBookingNotificationOutbox implements BookingNotificationOutbox {

  private final NotificationProperties properties;
  private final ManagementTokenCipher tokenCipher;
  private final GuesthouseNotificationConfigurationDao configurationDao;
  private final NotificationOutboxRepository outboxRepository;

  public DatabaseBookingNotificationOutbox(
      NotificationProperties properties,
      ManagementTokenCipher tokenCipher,
      GuesthouseNotificationConfigurationDao configurationDao,
      NotificationOutboxRepository outboxRepository) {
    this.properties = properties;
    this.tokenCipher = tokenCipher;
    this.configurationDao = configurationDao;
    this.outboxRepository = outboxRepository;
  }

  @Override
  public void enqueueBookingReceived(BookingRequest booking, String rawManagementToken) {
    if (!properties.enabled()) {
      return;
    }
    Instant now = Instant.now();
    String context = booking.getId().toString();
    EncryptedManagementToken encryptedToken = tokenCipher.encrypt(rawManagementToken, context);
    List<NotificationOutbox> jobs = new ArrayList<>();
    jobs.add(
        NotificationOutbox.guest(
            booking.getId(),
            normalizeEmail(booking.getContactEmail()),
            configurationDao.findPublicReplyTo(booking.getGuesthouseId()),
            booking.getPreferredLanguage(),
            encryptedToken,
            now));
    configurationDao
        .findActiveRecipients(booking.getGuesthouseId())
        .forEach(recipient -> jobs.add(NotificationOutbox.admin(booking.getId(), recipient, now)));
    outboxRepository.saveAll(jobs);
    outboxRepository.flush();
  }

  @Override
  public void enqueueBookingDecision(
      BookingRequest booking, BookingStatus decision, String guestMessage) {
    if (!properties.enabled()) {
      return;
    }
    NotificationType type =
        switch (decision) {
          case CONFIRMED -> NotificationType.BOOKING_CONFIRMED_GUEST;
          case REJECTED -> NotificationType.BOOKING_REJECTED_GUEST;
          default -> throw new IllegalArgumentException("Unsupported booking decision");
        };
    outboxRepository.saveAndFlush(
        NotificationOutbox.guestDecision(
            booking.getId(),
            type,
            normalizeEmail(booking.getContactEmail()),
            configurationDao.findPublicReplyTo(booking.getGuesthouseId()),
            booking.getPreferredLanguage(),
            guestMessage,
            Instant.now()));
  }

  private String normalizeEmail(String email) {
    return email.strip().toLowerCase(Locale.ROOT);
  }
}
