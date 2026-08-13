package com.bukovina.platform.accommodation.guesthouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "guesthouse_translation")
public class GuesthouseTranslation {

  @EmbeddedId private GuesthouseTranslationId id;

  @MapsId("guesthouseId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "guesthouse_id", nullable = false)
  private Guesthouse guesthouse;

  @Column(nullable = false, length = 160)
  private String name;

  @Column(name = "short_description", nullable = false, length = 500)
  private String shortDescription;

  @Column(nullable = false)
  private String description;

  @Column(name = "room_description", nullable = false)
  private String roomDescription;

  @Column(name = "history_title", length = 240)
  private String historyTitle;

  @Column(name = "history_text")
  private String historyText;

  @Version
  @Column(nullable = false)
  private long version;

  protected GuesthouseTranslation() {}

  public GuesthouseTranslation(Guesthouse guesthouse, String languageCode) {
    this.id = new GuesthouseTranslationId(guesthouse.getId(), languageCode);
    this.guesthouse = guesthouse;
  }

  public void updateContent(
      String name,
      String shortDescription,
      String description,
      String roomDescription,
      String historyTitle,
      String historyText) {
    this.name = name;
    this.shortDescription = shortDescription;
    this.description = description;
    this.roomDescription = roomDescription;
    this.historyTitle = historyTitle;
    this.historyText = historyText;
  }

  public String getLanguageCode() {
    return id.getLanguageCode();
  }

  public String getName() {
    return name;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public String getDescription() {
    return description;
  }

  public String getRoomDescription() {
    return roomDescription;
  }

  public String getHistoryTitle() {
    return historyTitle;
  }

  public String getHistoryText() {
    return historyText;
  }

  public long getVersion() {
    return version;
  }
}
