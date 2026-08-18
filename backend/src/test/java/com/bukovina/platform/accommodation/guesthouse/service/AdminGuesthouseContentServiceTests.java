package com.bukovina.platform.accommodation.guesthouse.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bukovina.platform.accommodation.guesthouse.dao.GuesthouseRepository;
import com.bukovina.platform.accommodation.guesthouse.dao.GuesthouseTranslationRepository;
import com.bukovina.platform.accommodation.guesthouse.dto.AdminGuesthouseTranslationUpdateRequest;
import com.bukovina.platform.accommodation.guesthouse.model.Guesthouse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class AdminGuesthouseContentServiceTests {

  @Test
  void reportsAConflictWhenConcurrentTranslationCreationViolatesThePrimaryKey() {
    GuesthouseRepository guesthouseRepository = mock(GuesthouseRepository.class);
    GuesthouseTranslationRepository translationRepository =
        mock(GuesthouseTranslationRepository.class);
    Guesthouse guesthouse = mock(Guesthouse.class);
    UUID guesthouseId = UUID.randomUUID();
    when(guesthouse.getId()).thenReturn(guesthouseId);
    when(guesthouseRepository.findById(guesthouseId)).thenReturn(Optional.of(guesthouse));
    when(translationRepository.findById(any())).thenReturn(Optional.empty());
    when(translationRepository.saveAndFlush(any()))
        .thenThrow(new DataIntegrityViolationException("duplicate translation"));
    AdminGuesthouseContentService service =
        new AdminGuesthouseContentService(guesthouseRepository, translationRepository);

    assertThrows(
        AdminGuesthouseContentConflictException.class,
        () -> service.update(guesthouseId, "en", validRequest()));
  }

  private AdminGuesthouseTranslationUpdateRequest validRequest() {
    return new AdminGuesthouseTranslationUpdateRequest(
        null,
        "Name",
        "Short description",
        "Description",
        "Room description",
        "Story label",
        "Story title",
        "Dining label",
        "Dining title",
        "Dining description",
        "Amenities",
        "Room types",
        "Pricing",
        "History label",
        "History title",
        "History text",
        "Gallery",
        "Gallery hint");
  }
}
