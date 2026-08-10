package com.bukovina.platform.accommodation.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "booking_status_history")
public class BookingStatusHistory {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "booking_request_id", nullable = false)
  private BookingRequest bookingRequest;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private BookingStatus status;

  @Column(name = "changed_at", nullable = false)
  private Instant changedAt;

  @Column(name = "changed_by", nullable = false, length = 160)
  private String changedBy;

  protected BookingStatusHistory() {}

  BookingStatusHistory(
      BookingRequest bookingRequest, BookingStatus status, Instant changedAt, String changedBy) {
    this.id = UUID.randomUUID();
    this.bookingRequest = Objects.requireNonNull(bookingRequest);
    this.status = Objects.requireNonNull(status);
    this.changedAt = Objects.requireNonNull(changedAt);
    this.changedBy = Objects.requireNonNull(changedBy);
  }

  public UUID getId() {
    return id;
  }

  public BookingStatus getStatus() {
    return status;
  }

  public Instant getChangedAt() {
    return changedAt;
  }

  public String getChangedBy() {
    return changedBy;
  }
}
