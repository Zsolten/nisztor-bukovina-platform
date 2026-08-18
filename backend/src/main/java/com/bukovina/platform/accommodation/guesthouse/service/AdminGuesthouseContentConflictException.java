package com.bukovina.platform.accommodation.guesthouse.service;

import com.bukovina.platform.accommodation.guesthouse.dto.AdminGuesthouseTranslationResponse;

public class AdminGuesthouseContentConflictException extends RuntimeException {

  private final AdminGuesthouseTranslationResponse currentContent;

  public AdminGuesthouseContentConflictException(
      AdminGuesthouseTranslationResponse currentContent) {
    this.currentContent = currentContent;
  }

  public AdminGuesthouseTranslationResponse getCurrentContent() {
    return currentContent;
  }
}
