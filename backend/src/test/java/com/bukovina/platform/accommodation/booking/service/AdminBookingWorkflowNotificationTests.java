package com.bukovina.platform.accommodation.booking.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bukovina.platform.accommodation.booking.dao.BookingRequestRepository;
import com.bukovina.platform.accommodation.booking.model.BookingRequest;
import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdminBookingWorkflowNotificationTests {

  private final BookingRequestRepository repository = mock(BookingRequestRepository.class);
  private final BookingNotificationOutbox outbox = mock(BookingNotificationOutbox.class);
  private final AdminBookingWorkflowService service =
      new AdminBookingWorkflowService(repository, outbox);

  @Test
  void enqueuesTheNormalizedGuestMessageForAConfirmedBooking() {
    UUID bookingId = UUID.randomUUID();
    BookingRequest booking = mock(BookingRequest.class);
    when(repository.findForUpdateById(bookingId)).thenReturn(Optional.of(booking));

    service.changeStatus(
        bookingId, BookingStatus.CONFIRMED, "  Szeretettel várjuk!  ", "ADMIN:owner");

    verify(booking)
        .transitionTo(
            org.mockito.ArgumentMatchers.eq(BookingStatus.CONFIRMED),
            org.mockito.ArgumentMatchers.any(Instant.class),
            org.mockito.ArgumentMatchers.eq("ADMIN:owner"));
    verify(outbox).enqueueBookingDecision(booking, BookingStatus.CONFIRMED, "Szeretettel várjuk!");
    verify(repository).flush();
  }

  @Test
  void doesNotNotifyTheGuestWhenReviewOnlyStarts() {
    UUID bookingId = UUID.randomUUID();
    BookingRequest booking = mock(BookingRequest.class);
    when(repository.findForUpdateById(bookingId)).thenReturn(Optional.of(booking));

    service.changeStatus(bookingId, BookingStatus.UNDER_REVIEW, null, "ADMIN:owner");

    verify(outbox, never())
        .enqueueBookingDecision(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
  }

  @Test
  void rejectsAnOversizedGuestMessageBeforeChangingStatus() {
    UUID bookingId = UUID.randomUUID();
    BookingRequest booking = mock(BookingRequest.class);
    when(repository.findForUpdateById(bookingId)).thenReturn(Optional.of(booking));

    assertThrows(
        AdminBookingWorkflowValidationException.class,
        () ->
            service.changeStatus(
                bookingId, BookingStatus.REJECTED, "x".repeat(2001), "ADMIN:owner"));

    verify(booking, never())
        .transitionTo(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
    verify(outbox, never())
        .enqueueBookingDecision(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
  }
}
