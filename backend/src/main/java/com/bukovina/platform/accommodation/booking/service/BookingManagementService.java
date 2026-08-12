package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.dao.BookingManagementQueryDao;
import com.bukovina.platform.accommodation.booking.dao.BookingRequestRepository;
import com.bukovina.platform.accommodation.booking.dto.BookingManagementResponse;
import com.bukovina.platform.accommodation.booking.model.BookingRequest;
import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingManagementService {

  private static final String TOKEN_PATTERN = "[A-Za-z0-9_-]{43}";
  private static final Set<String> SUPPORTED_LANGUAGES = Set.of("hu", "ro", "en");

  private final BookingRequestRepository repository;
  private final BookingManagementQueryDao queryDao;

  public BookingManagementService(
      BookingRequestRepository repository, BookingManagementQueryDao queryDao) {
    this.repository = repository;
    this.queryDao = queryDao;
  }

  @Transactional(readOnly = true)
  public BookingManagementResponse get(String rawToken, String language) {
    String tokenHash = tokenHash(rawToken);
    BookingRequest booking =
        repository
            .findByManagementTokenHash(tokenHash)
            .filter(candidate -> candidate.hasUsableManagementTokenAt(Instant.now()))
            .orElseThrow(BookingManagementNotFoundException::new);
    String resolvedLanguage = SUPPORTED_LANGUAGES.contains(language) ? language : "hu";
    BookingManagementView view =
        queryDao
            .findById(booking.getId(), resolvedLanguage)
            .orElseThrow(BookingManagementNotFoundException::new);
    return BookingManagementResponse.from(view, booking.getStatus());
  }

  @Transactional
  public void cancel(String rawToken) {
    String tokenHash = tokenHash(rawToken);
    BookingRequest booking =
        repository
            .findForUpdateByManagementTokenHash(tokenHash)
            .filter(candidate -> candidate.hasUsableManagementTokenAt(Instant.now()))
            .orElseThrow(BookingManagementNotFoundException::new);
    if (booking.getStatus() != BookingStatus.RECEIVED
        && booking.getStatus() != BookingStatus.UNDER_REVIEW) {
      throw new BookingCancellationNotAllowedException();
    }
    Instant now = Instant.now();
    booking.transitionTo(BookingStatus.CANCELLED, now, "GUEST_MANAGEMENT_LINK");
    booking.revokeManagementToken(now);
    repository.flush();
  }

  private String tokenHash(String rawToken) {
    if (rawToken == null || !rawToken.matches(TOKEN_PATTERN)) {
      throw new BookingManagementNotFoundException();
    }
    return BookingHashing.sha256(rawToken);
  }
}
