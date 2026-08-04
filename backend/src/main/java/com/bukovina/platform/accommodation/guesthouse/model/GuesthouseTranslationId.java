package com.bukovina.platform.accommodation.guesthouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class GuesthouseTranslationId implements Serializable {

  @Column(name = "guesthouse_id")
  private UUID guesthouseId;

  @Column(name = "language_code", length = 2)
  private String languageCode;

  protected GuesthouseTranslationId() {}

  public UUID getGuesthouseId() {
    return guesthouseId;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GuesthouseTranslationId that)) {
      return false;
    }
    return Objects.equals(guesthouseId, that.guesthouseId)
        && Objects.equals(languageCode, that.languageCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guesthouseId, languageCode);
  }
}
