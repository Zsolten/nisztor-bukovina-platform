package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.dto.CreateBookingRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class BookingContactValidator {

  static final int NAME_MAX_LENGTH = 160;
  static final int EMAIL_MAX_LENGTH = 320;
  static final int PHONE_MAX_LENGTH = 40;
  static final int NOTE_MAX_LENGTH = 2000;
  private static final BigDecimal MAX_ACCEPTED_TOTAL = new BigDecimal("9999999999.99");
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9][0-9 ()-]{6,38}$");
  private static final Set<String> LANGUAGES = Set.of("hu", "ro", "en");

  public ValidatedContact validate(CreateBookingRequest request) {
    List<BookingProblem> problems = new ArrayList<>();
    String name = normalizeSpaces(request.contactName());
    String email =
        request.contactEmail() == null
            ? null
            : request.contactEmail().strip().toLowerCase(Locale.ROOT);
    String phone = request.contactPhone() == null ? null : request.contactPhone().strip();
    String language =
        request.preferredLanguage() == null ? null : request.preferredLanguage().strip();
    String note = request.note() == null ? null : request.note().strip();

    requiredAndLength(name, "contactName", NAME_MAX_LENGTH, problems);
    requiredAndLength(email, "contactEmail", EMAIL_MAX_LENGTH, problems);
    if (email != null
        && email.length() <= EMAIL_MAX_LENGTH
        && !EMAIL_PATTERN.matcher(email).matches()) {
      problems.add(problem("INVALID_EMAIL", "contactEmail", "email"));
    }
    requiredAndLength(phone, "contactPhone", PHONE_MAX_LENGTH, problems);
    if (phone != null
        && phone.length() <= PHONE_MAX_LENGTH
        && !PHONE_PATTERN.matcher(phone).matches()) {
      problems.add(problem("INVALID_PHONE", "contactPhone", "internationalPhone"));
    }
    if (language == null || language.isBlank()) {
      problems.add(problem("PREFERRED_LANGUAGE_REQUIRED", "preferredLanguage", "required"));
    } else if (!LANGUAGES.contains(language)) {
      problems.add(problem("UNSUPPORTED_LANGUAGE", "preferredLanguage", "supportedLanguage"));
    }
    if (note != null && note.length() > NOTE_MAX_LENGTH) {
      problems.add(problem("TEXT_TOO_LONG", "note", "maxLength"));
    }
    if (request.acceptedTotal() == null) {
      problems.add(problem("ACCEPTED_TOTAL_REQUIRED", "acceptedTotal", "required"));
    } else if (request.acceptedTotal().signum() < 0
        || request.acceptedTotal().stripTrailingZeros().scale() > 2
        || request.acceptedTotal().compareTo(MAX_ACCEPTED_TOTAL) > 0) {
      problems.add(problem("INVALID_ACCEPTED_TOTAL", "acceptedTotal", "nonNegative"));
    }

    if (!problems.isEmpty()) {
      throw new BookingValidationException(problems);
    }
    return new ValidatedContact(
        name, email, phone, language, note == null || note.isBlank() ? null : note);
  }

  private void requiredAndLength(
      String value, String field, int maxLength, List<BookingProblem> problems) {
    if (value == null || value.isBlank()) {
      problems.add(problem("CONTACT_FIELD_REQUIRED", field, "required"));
    } else if (value.length() > maxLength) {
      problems.add(problem("TEXT_TOO_LONG", field, "maxLength"));
    }
  }

  private String normalizeSpaces(String value) {
    return value == null ? null : value.strip().replaceAll("\\s+", " ");
  }

  private BookingProblem problem(String code, String field, String rule) {
    return new BookingProblem(code, field, rule);
  }
}
