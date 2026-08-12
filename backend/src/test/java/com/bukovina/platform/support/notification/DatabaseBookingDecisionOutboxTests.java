package com.bukovina.platform.support.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bukovina.platform.accommodation.booking.model.BookingRequest;
import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DatabaseBookingDecisionOutboxTests {

  @Test
  void snapshotsTheGuestRecipientLanguageReplyToAndMessage() {
    NotificationProperties properties = mock(NotificationProperties.class);
    ManagementTokenCipher cipher = mock(ManagementTokenCipher.class);
    GuesthouseNotificationConfigurationDao configuration =
        mock(GuesthouseNotificationConfigurationDao.class);
    NotificationOutboxRepository repository = mock(NotificationOutboxRepository.class);
    BookingRequest booking = mock(BookingRequest.class);
    UUID bookingId = UUID.randomUUID();
    UUID guesthouseId = UUID.randomUUID();
    when(properties.enabled()).thenReturn(true);
    when(booking.getId()).thenReturn(bookingId);
    when(booking.getGuesthouseId()).thenReturn(guesthouseId);
    when(booking.getContactEmail()).thenReturn("GUEST@EXAMPLE.COM");
    when(booking.getPreferredLanguage()).thenReturn("ro");
    when(configuration.findPublicReplyTo(guesthouseId)).thenReturn("nisztorpanzio@gmail.com");
    DatabaseBookingNotificationOutbox outbox =
        new DatabaseBookingNotificationOutbox(properties, cipher, configuration, repository);

    outbox.enqueueBookingDecision(booking, BookingStatus.REJECTED, "Nu mai avem camere.");

    ArgumentCaptor<NotificationOutbox> job = ArgumentCaptor.forClass(NotificationOutbox.class);
    verify(repository).saveAndFlush(job.capture());
    assertEquals(bookingId, job.getValue().getBookingRequestId());
    assertEquals(NotificationType.BOOKING_REJECTED_GUEST, job.getValue().getNotificationType());
    assertEquals("guest@example.com", job.getValue().getRecipient());
    assertEquals("nisztorpanzio@gmail.com", job.getValue().getReplyTo());
    assertEquals("ro", job.getValue().getLanguageCode());
    assertEquals("Nu mai avem camere.", job.getValue().getGuestMessage());
  }
}
