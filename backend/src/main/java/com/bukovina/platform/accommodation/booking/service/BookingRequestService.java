package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.dao.BookingIdempotencyGuard;
import com.bukovina.platform.accommodation.booking.dao.BookingRequestRepository;
import com.bukovina.platform.accommodation.booking.dto.BookingPriceBreakdownResponse;
import com.bukovina.platform.accommodation.booking.dto.BookingQuoteResponse;
import com.bukovina.platform.accommodation.booking.dto.BookingRequestCreatedResponse;
import com.bukovina.platform.accommodation.booking.dto.CreateBookingRequest;
import com.bukovina.platform.accommodation.booking.model.BookingPriceBreakdown;
import com.bukovina.platform.accommodation.booking.model.BookingRequest;
import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingRequestService {

  private static final int IDEMPOTENCY_KEY_MAX_LENGTH = 128;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private final BookingValidator validator;
  private final BookingContactValidator contactValidator;
  private final BookingPriceCalculator calculator;
  private final BookingRequestRepository repository;
  private final BookingIdempotencyGuard idempotencyGuard;
  private final ManagementTokenGenerator managementTokenGenerator;
  private final BookingNotificationOutbox notificationOutbox;

  public BookingRequestService(
      BookingValidator validator,
      BookingContactValidator contactValidator,
      BookingPriceCalculator calculator,
      BookingRequestRepository repository,
      BookingIdempotencyGuard idempotencyGuard,
      ManagementTokenGenerator managementTokenGenerator,
      BookingNotificationOutbox notificationOutbox) {
    this.validator = validator;
    this.contactValidator = contactValidator;
    this.calculator = calculator;
    this.repository = repository;
    this.idempotencyGuard = idempotencyGuard;
    this.managementTokenGenerator = managementTokenGenerator;
    this.notificationOutbox = notificationOutbox;
  }

  @Transactional
  public BookingRequestCreatedResponse create(CreateBookingRequest request, String idempotencyKey) {
    validateIdempotencyKey(idempotencyKey);
    if (request == null) {
      throw new BookingValidationException("REQUEST_REQUIRED", "request", "required");
    }
    String keyHash = BookingHashing.sha256(idempotencyKey.strip());
    String fingerprint = fingerprint(request);

    idempotencyGuard.lock(keyHash);
    BookingRequest existing = repository.findByIdempotencyKeyHash(keyHash).orElse(null);
    if (existing != null) {
      if (!existing.getRequestFingerprint().equals(fingerprint)) {
        throw new BookingConflictException("IDEMPOTENCY_KEY_REUSED", null);
      }
      return createdResponse(existing);
    }

    ValidatedBooking booking = validator.validate(request);
    ValidatedContact contact = contactValidator.validate(request);
    BookingQuoteResponse quote = calculator.calculate(booking, contact.preferredLanguage());
    if (money(request.acceptedTotal()).compareTo(quote.priceBreakdown().totalPayable()) != 0) {
      throw new BookingConflictException("BOOKING_PRICE_CHANGED", quote);
    }

    Instant now = Instant.now();
    GeneratedManagementToken managementToken = managementTokenGenerator.generate();
    BookingRequest entity =
        new BookingRequest(
            booking.guesthouseId(),
            publicReference(),
            keyHash,
            fingerprint,
            booking.checkInDate(),
            booking.checkOutDate(),
            booking.adults(),
            booking.childrenAge3to10(),
            booking.childrenAge0to3(),
            booking.breakfastParticipants(),
            booking.dinnerParticipants(),
            contact.name(),
            contact.email(),
            contact.phone(),
            contact.preferredLanguage(),
            contact.note(),
            toEntityBreakdown(quote.priceBreakdown()),
            managementToken.tokenHash(),
            now.plus(30, ChronoUnit.DAYS));
    booking
        .roomSelections()
        .forEach(room -> entity.addRoomSelection(room.roomTypeId(), room.quantity()));
    entity.addStatusHistory(BookingStatus.RECEIVED, now, "SYSTEM");
    BookingRequest saved = repository.saveAndFlush(entity);
    notificationOutbox.enqueueBookingReceived(saved, managementToken.rawToken());
    return createdResponse(saved);
  }

  private void validateIdempotencyKey(String idempotencyKey) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new BookingValidationException(
          "IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key", "required");
    }
    if (idempotencyKey.strip().length() > IDEMPOTENCY_KEY_MAX_LENGTH) {
      throw new BookingValidationException(
          "IDEMPOTENCY_KEY_TOO_LONG", "Idempotency-Key", "maxLength");
    }
  }

  private String fingerprint(CreateBookingRequest request) {
    StringBuilder canonical =
        new StringBuilder()
            .append(request.guesthouseId())
            .append('|')
            .append(request.checkInDate())
            .append('|')
            .append(request.checkOutDate())
            .append('|')
            .append(request.adults())
            .append('|')
            .append(request.childrenAge3to10())
            .append('|')
            .append(request.childrenAge0to3())
            .append('|')
            .append(request.services() == null ? null : request.services().breakfastParticipants())
            .append('|')
            .append(request.services() == null ? null : request.services().dinnerParticipants());
    if (request.roomSelections() != null) {
      request.roomSelections().stream()
          .sorted(
              Comparator.comparing(
                  room ->
                      room == null || room.roomTypeId() == null
                          ? ""
                          : room.roomTypeId().toString()))
          .forEach(
              room ->
                  canonical
                      .append('|')
                      .append(room == null ? null : room.roomTypeId())
                      .append(':')
                      .append(room == null ? null : room.quantity()));
    }
    canonical
        .append('|')
        .append(normalizeSpaces(request.contactName()))
        .append('|')
        .append(normalizeEmail(request.contactEmail()))
        .append('|')
        .append(strip(request.contactPhone()))
        .append('|')
        .append(strip(request.preferredLanguage()))
        .append('|')
        .append(strip(request.note()))
        .append('|')
        .append(
            request.acceptedTotal() == null
                ? null
                : request.acceptedTotal().stripTrailingZeros().toPlainString());
    return BookingHashing.sha256(canonical.toString());
  }

  private String normalizeSpaces(String value) {
    return value == null ? null : value.strip().replaceAll("\\s+", " ");
  }

  private String normalizeEmail(String value) {
    String stripped = strip(value);
    return stripped == null ? null : stripped.toLowerCase(Locale.ROOT);
  }

  private String strip(String value) {
    return value == null ? null : value.strip();
  }

  private BookingPriceBreakdown toEntityBreakdown(BookingPriceBreakdownResponse breakdown) {
    return new BookingPriceBreakdown(
        breakdown.accommodationTotal(),
        breakdown.singleRoomSurcharge(),
        breakdown.breakfastTotal(),
        breakdown.dinnerTotal(),
        breakdown.totalPayable());
  }

  private BookingRequestCreatedResponse createdResponse(BookingRequest booking) {
    return new BookingRequestCreatedResponse(
        booking.getPublicReference(),
        booking.getStatus(),
        booking.getCurrency(),
        booking.getNights(),
        booking.getAdults() + booking.getChildrenAge3to10() + booking.getChildrenAge0to3(),
        booking.getPriceBreakdown().getTotalPayable(),
        true);
  }

  private String publicReference() {
    byte[] bytes = new byte[8];
    SECURE_RANDOM.nextBytes(bytes);
    return "NB-" + HexFormat.of().withUpperCase().formatHex(bytes);
  }

  private BigDecimal money(BigDecimal amount) {
    return amount.setScale(2, RoundingMode.HALF_UP);
  }
}
