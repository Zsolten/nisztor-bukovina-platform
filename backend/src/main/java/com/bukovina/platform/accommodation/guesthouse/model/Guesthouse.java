package com.bukovina.platform.accommodation.guesthouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "guesthouse")
public class Guesthouse {

  @Id private UUID id;

  @Column(nullable = false, unique = true, length = 80)
  private String slug;

  @Column(name = "room_count", nullable = false)
  private int roomCount;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @OneToMany(mappedBy = "guesthouse")
  private List<GuesthouseTranslation> translations = new ArrayList<>();

  @OneToMany(mappedBy = "guesthouse")
  @OrderBy("displayOrder ASC")
  private List<GuesthouseImage> images = new ArrayList<>();

  protected Guesthouse() {}

  public UUID getId() {
    return id;
  }

  public String getSlug() {
    return slug;
  }

  public int getRoomCount() {
    return roomCount;
  }

  public boolean isActive() {
    return active;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public List<GuesthouseTranslation> getTranslations() {
    return List.copyOf(translations);
  }

  public List<GuesthouseImage> getImages() {
    return List.copyOf(images);
  }
}
