package com.bukovina.platform.accommodation.booking.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "booking_request",
    indexes = {
      @Index(name = "idx_booking_request_guesthouse_id", columnList = "guesthouse_id"),
      @Index(name = "idx_booking_request_status", columnList = "status"),
      @Index(name = "idx_booking_request_contact_email", columnList = "contact_email"),
      @Index(name = "idx_booking_request_created_at", columnList = "created_at")
    })
public class BookingRequest {

  @Id private UUID id;

  @Column(name = "guesthouse_id", nullable = false)
  private UUID guesthouseId;

  @Column(name = "check_in_date", nullable = false)
  private LocalDate checkInDate;

  @Column(name = "check_out_date", nullable = false)
  private LocalDate checkOutDate;

  @Column(nullable = false)
  private int adults;

  @Column(name = "children_age_3_to_10", nullable = false)
  private int childrenAge3to10;

  @Column(name = "children_age_0_to_3", nullable = false)
  private int childrenAge0to3;

  @Column(name = "contact_name", nullable = false, length = 160)
  private String contactName;

  @Column(name = "contact_email", nullable = false, length = 320)
  private String contactEmail;

  @Column(name = "contact_phone", nullable = false, length = 40)
  private String contactPhone;

  @Column(name = "preferred_language", nullable = false, length = 2)
  private String preferredLanguage;

  @Column private String note;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private BookingStatus status;

  @Embedded private BookingPriceBreakdown priceBreakdown;

  @Column(name = "management_token_hash", nullable = false, unique = true, length = 128)
  private String managementTokenHash;

  @Column(name = "management_token_expires_at", nullable = false)
  private Instant managementTokenExpiresAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "bookingRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BookingRoomSelection> roomSelections = new ArrayList<>();

  @OneToMany(mappedBy = "bookingRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("changedAt ASC")
  private List<BookingStatusHistory> statusHistory = new ArrayList<>();

  protected BookingRequest() {}

  public BookingRequest(
      UUID guesthouseId,
      LocalDate checkInDate,
      LocalDate checkOutDate,
      int adults,
      int childrenAge3to10,
      int childrenAge0to3,
      String contactName,
      String contactEmail,
      String contactPhone,
      String preferredLanguage,
      String note,
      BookingStatus status,
      BookingPriceBreakdown priceBreakdown,
      String managementTokenHash,
      Instant managementTokenExpiresAt) {
    this.id = UUID.randomUUID();
    this.guesthouseId = Objects.requireNonNull(guesthouseId);
    this.checkInDate = Objects.requireNonNull(checkInDate);
    this.checkOutDate = Objects.requireNonNull(checkOutDate);
    this.adults = adults;
    this.childrenAge3to10 = childrenAge3to10;
    this.childrenAge0to3 = childrenAge0to3;
    this.contactName = Objects.requireNonNull(contactName);
    this.contactEmail = Objects.requireNonNull(contactEmail);
    this.contactPhone = Objects.requireNonNull(contactPhone);
    this.preferredLanguage = Objects.requireNonNull(preferredLanguage);
    this.note = note;
    this.status = Objects.requireNonNull(status);
    this.priceBreakdown = Objects.requireNonNull(priceBreakdown);
    this.managementTokenHash = Objects.requireNonNull(managementTokenHash);
    this.managementTokenExpiresAt = Objects.requireNonNull(managementTokenExpiresAt);
  }

  public void addRoomSelection(UUID roomTypeId, int quantity) {
    roomSelections.add(new BookingRoomSelection(this, roomTypeId, quantity));
  }

  public void addStatusHistory(BookingStatus recordedStatus, Instant changedAt, String changedBy) {
    statusHistory.add(new BookingStatusHistory(this, recordedStatus, changedAt, changedBy));
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public UUID getGuesthouseId() {
    return guesthouseId;
  }

  public LocalDate getCheckInDate() {
    return checkInDate;
  }

  public LocalDate getCheckOutDate() {
    return checkOutDate;
  }

  public long getNights() {
    return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
  }

  public int getAdults() {
    return adults;
  }

  public int getChildrenAge3to10() {
    return childrenAge3to10;
  }

  public int getChildrenAge0to3() {
    return childrenAge0to3;
  }

  public String getContactName() {
    return contactName;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public String getContactPhone() {
    return contactPhone;
  }

  public String getPreferredLanguage() {
    return preferredLanguage;
  }

  public String getNote() {
    return note;
  }

  public BookingStatus getStatus() {
    return status;
  }

  public BookingPriceBreakdown getPriceBreakdown() {
    return priceBreakdown;
  }

  public String getManagementTokenHash() {
    return managementTokenHash;
  }

  public Instant getManagementTokenExpiresAt() {
    return managementTokenExpiresAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public List<BookingRoomSelection> getRoomSelections() {
    return List.copyOf(roomSelections);
  }

  public List<BookingStatusHistory> getStatusHistory() {
    return List.copyOf(statusHistory);
  }
}
