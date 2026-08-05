package com.bukovina.platform.accommodation.guesthouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "guesthouse_image")
public class GuesthouseImage {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "guesthouse_id", nullable = false)
  private Guesthouse guesthouse;

  @Column(nullable = false, length = 500)
  private String path;

  @Column(name = "alt_text", nullable = false, length = 300)
  private String altText;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @Column(nullable = false)
  private boolean cover;

  protected GuesthouseImage() {}

  public String getPath() {
    return path;
  }

  public String getAltText() {
    return altText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public boolean isCover() {
    return cover;
  }
}
