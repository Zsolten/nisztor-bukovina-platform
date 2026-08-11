package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.dao.BookingRequestRepository;
import com.bukovina.platform.accommodation.booking.model.BookingRequest;
import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBookingWorkflowService {

  private static final int INTERNAL_NOTE_MAX_LENGTH = 4000;
  private static final Set<BookingStatus> ADMIN_WORKFLOW_TARGETS =
      EnumSet.of(BookingStatus.UNDER_REVIEW, BookingStatus.CONFIRMED, BookingStatus.REJECTED);

  private final BookingRequestRepository bookingRepository;

  public AdminBookingWorkflowService(BookingRequestRepository bookingRepository) {
    this.bookingRepository = bookingRepository;
  }

  @Transactional
  public void changeStatus(UUID bookingId, BookingStatus requestedStatus, String actor) {
    if (requestedStatus == null) {
      throw new AdminBookingWorkflowValidationException("BOOKING_STATUS_REQUIRED");
    }
    if (!ADMIN_WORKFLOW_TARGETS.contains(requestedStatus)) {
      throw new AdminBookingWorkflowValidationException("UNSUPPORTED_ADMIN_BOOKING_STATUS");
    }
    BookingRequest booking = findForUpdate(bookingId);
    booking.transitionTo(requestedStatus, Instant.now(), actor);
    bookingRepository.flush();
  }

  @Transactional
  public void updateInternalNote(UUID bookingId, String internalNote) {
    String normalizedNote = normalizeInternalNote(internalNote);
    BookingRequest booking = findForUpdate(bookingId);
    booking.updateInternalNote(normalizedNote);
    bookingRepository.flush();
  }

  private BookingRequest findForUpdate(UUID bookingId) {
    return bookingRepository
        .findForUpdateById(bookingId)
        .orElseThrow(() -> new AdminBookingNotFoundException(bookingId));
  }

  private String normalizeInternalNote(String internalNote) {
    if (internalNote == null) {
      return null;
    }
    String normalized = internalNote.strip();
    if (normalized.length() > INTERNAL_NOTE_MAX_LENGTH) {
      throw new AdminBookingWorkflowValidationException("INTERNAL_NOTE_TOO_LONG");
    }
    return normalized.isEmpty() ? null : normalized;
  }
}
