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

  @Column(name = "story_eyebrow", nullable = false, length = 240)
  private String storyEyebrow;

  @Column(name = "story_title", nullable = false, length = 240)
  private String storyTitle;

  @Column(name = "dining_eyebrow", nullable = false, length = 240)
  private String diningEyebrow;

  @Column(name = "dining_title", nullable = false, length = 240)
  private String diningTitle;

  @Column(name = "dining_description", nullable = false, length = 1000)
  private String diningDescription;

  @Column(name = "amenities_title", nullable = false, length = 240)
  private String amenitiesTitle;

  @Column(name = "room_types_title", nullable = false, length = 240)
  private String roomTypesTitle;

  @Column(name = "pricing_title", nullable = false, length = 240)
  private String pricingTitle;

  @Column(name = "history_eyebrow", nullable = false, length = 240)
  private String historyEyebrow;

  @Column(name = "history_title", length = 240)
  private String historyTitle;

  @Column(name = "history_text")
  private String historyText;

  @Column(name = "gallery_title", nullable = false, length = 240)
  private String galleryTitle;

  @Column(name = "gallery_hint", nullable = false, length = 500)
  private String galleryHint;

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
      String storyEyebrow,
      String storyTitle,
      String diningEyebrow,
      String diningTitle,
      String diningDescription,
      String amenitiesTitle,
      String roomTypesTitle,
      String pricingTitle,
      String historyEyebrow,
      String historyTitle,
      String historyText,
      String galleryTitle,
      String galleryHint) {
    this.name = name;
    this.shortDescription = shortDescription;
    this.description = description;
    this.roomDescription = roomDescription;
    this.storyEyebrow = storyEyebrow;
    this.storyTitle = storyTitle;
    this.diningEyebrow = diningEyebrow;
    this.diningTitle = diningTitle;
    this.diningDescription = diningDescription;
    this.amenitiesTitle = amenitiesTitle;
    this.roomTypesTitle = roomTypesTitle;
    this.pricingTitle = pricingTitle;
    this.historyEyebrow = historyEyebrow;
    this.historyTitle = historyTitle;
    this.historyText = historyText;
    this.galleryTitle = galleryTitle;
    this.galleryHint = galleryHint;
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

  public String getStoryEyebrow() {
    return storyEyebrow;
  }

  public String getStoryTitle() {
    return storyTitle;
  }

  public String getDiningEyebrow() {
    return diningEyebrow;
  }

  public String getDiningTitle() {
    return diningTitle;
  }

  public String getDiningDescription() {
    return diningDescription;
  }

  public String getAmenitiesTitle() {
    return amenitiesTitle;
  }

  public String getRoomTypesTitle() {
    return roomTypesTitle;
  }

  public String getPricingTitle() {
    return pricingTitle;
  }

  public String getHistoryEyebrow() {
    return historyEyebrow;
  }

  public String getHistoryTitle() {
    return historyTitle;
  }

  public String getHistoryText() {
    return historyText;
  }

  public String getGalleryTitle() {
    return galleryTitle;
  }

  public String getGalleryHint() {
    return galleryHint;
  }

  public long getVersion() {
    return version;
  }
}
