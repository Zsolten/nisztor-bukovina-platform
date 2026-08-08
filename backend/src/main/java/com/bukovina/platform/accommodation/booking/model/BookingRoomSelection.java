package com.bukovina.platform.accommodation.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "booking_room_selection",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_booking_room_selection_booking_room_type",
            columnNames = {"booking_request_id", "room_type_id"}))
public class BookingRoomSelection {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "booking_request_id", nullable = false)
  private BookingRequest bookingRequest;

  @Column(name = "room_type_id", nullable = false)
  private UUID roomTypeId;

  @Column(nullable = false)
  private int quantity;

  protected BookingRoomSelection() {}

  BookingRoomSelection(BookingRequest bookingRequest, UUID roomTypeId, int quantity) {
    this.id = UUID.randomUUID();
    this.bookingRequest = Objects.requireNonNull(bookingRequest);
    this.roomTypeId = Objects.requireNonNull(roomTypeId);
    this.quantity = quantity;
  }

  public UUID getId() {
    return id;
  }

  public UUID getRoomTypeId() {
    return roomTypeId;
  }

  public int getQuantity() {
    return quantity;
  }
}
