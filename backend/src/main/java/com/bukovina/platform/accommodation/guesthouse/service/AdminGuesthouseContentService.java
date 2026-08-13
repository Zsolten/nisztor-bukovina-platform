package com.bukovina.platform.accommodation.guesthouse.service;

import com.bukovina.platform.accommodation.guesthouse.dao.GuesthouseRepository;
import com.bukovina.platform.accommodation.guesthouse.dao.GuesthouseTranslationRepository;
import com.bukovina.platform.accommodation.guesthouse.dto.AdminGuesthouseContentResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.AdminGuesthouseTranslationResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.AdminGuesthouseTranslationUpdateRequest;
import com.bukovina.platform.accommodation.guesthouse.model.Guesthouse;
import com.bukovina.platform.accommodation.guesthouse.model.GuesthouseTranslation;
import com.bukovina.platform.accommodation.guesthouse.model.GuesthouseTranslationId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminGuesthouseContentService {

  private static final List<String> LANGUAGES = List.of("hu", "ro", "en");
  private static final Set<String> SUPPORTED_LANGUAGES = Set.copyOf(LANGUAGES);

  private final GuesthouseRepository guesthouseRepository;
  private final GuesthouseTranslationRepository translationRepository;

  public AdminGuesthouseContentService(
      GuesthouseRepository guesthouseRepository,
      GuesthouseTranslationRepository translationRepository) {
    this.guesthouseRepository = guesthouseRepository;
    this.translationRepository = translationRepository;
  }

  @Transactional(readOnly = true)
  public List<AdminGuesthouseContentResponse> list() {
    return guesthouseRepository.findAllByOrderByDisplayOrderAsc().stream()
        .map(this::toContentResponse)
        .toList();
  }

  @Transactional
  public AdminGuesthouseTranslationResponse update(
      UUID guesthouseId, String language, AdminGuesthouseTranslationUpdateRequest request) {
    validateLanguage(language);
    Guesthouse guesthouse =
        guesthouseRepository
            .findById(guesthouseId)
            .orElseThrow(AdminGuesthouseContentNotFoundException::new);
    GuesthouseTranslationId translationId = new GuesthouseTranslationId(guesthouseId, language);
    GuesthouseTranslation translation = translationRepository.findById(translationId).orElse(null);

    if (translation == null) {
      if (request.version() != null) {
        throw new AdminGuesthouseContentConflictException(null);
      }
      translation = new GuesthouseTranslation(guesthouse, language);
    } else if (request.version() == null || translation.getVersion() != request.version()) {
      throw new AdminGuesthouseContentConflictException(toTranslationResponse(translation));
    }

    translation.updateContent(
        request.name().trim(),
        request.shortDescription().trim(),
        request.description().trim(),
        request.roomDescription().trim(),
        request.historyTitle().trim(),
        request.historyText().trim());

    try {
      return toTranslationResponse(translationRepository.saveAndFlush(translation));
    } catch (OptimisticLockingFailureException exception) {
      throw new AdminGuesthouseContentConflictException(null);
    }
  }

  private void validateLanguage(String language) {
    if (!SUPPORTED_LANGUAGES.contains(language)) {
      throw new AdminGuesthouseContentValidationException("UNSUPPORTED_CONTENT_LANGUAGE");
    }
  }

  private AdminGuesthouseContentResponse toContentResponse(Guesthouse guesthouse) {
    List<AdminGuesthouseTranslationResponse> translations =
        LANGUAGES.stream().map(language -> translationFor(guesthouse, language)).toList();
    return new AdminGuesthouseContentResponse(
        guesthouse.getId(), guesthouse.getSlug(), guesthouse.isActive(), translations);
  }

  private AdminGuesthouseTranslationResponse translationFor(
      Guesthouse guesthouse, String language) {
    return guesthouse.getTranslations().stream()
        .filter(translation -> translation.getLanguageCode().equals(language))
        .findFirst()
        .map(this::toTranslationResponse)
        .orElseGet(() -> emptyTranslation(language));
  }

  private AdminGuesthouseTranslationResponse emptyTranslation(String language) {
    return new AdminGuesthouseTranslationResponse(language, null, "", "", "", "", "", "");
  }

  private AdminGuesthouseTranslationResponse toTranslationResponse(
      GuesthouseTranslation translation) {
    return new AdminGuesthouseTranslationResponse(
        translation.getLanguageCode(),
        translation.getVersion(),
        translation.getName(),
        translation.getShortDescription(),
        translation.getDescription(),
        translation.getRoomDescription(),
        translation.getHistoryTitle(),
        translation.getHistoryText());
  }
}
