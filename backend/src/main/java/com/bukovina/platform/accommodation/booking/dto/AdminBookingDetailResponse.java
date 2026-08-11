package com.bukovina.platform.accommodation.booking.dto;

import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import com.bukovina.platform.accommodation.booking.service.AdminBookingDetailView;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public record AdminBookingDetailResponse(
    UUID id,
    String publicReference,
    Guesthouse guesthouse,
    Stay stay,
    Contact contact,
    Services services,
    List<RoomSelection> rooms,
    PriceSnapshot priceSnapshot,
    BookingStatus status,
    List<StatusHistory> statusHistory,
    String guestNote,
    String internalNote,
    Instant createdAt,
    Instant updatedAt) {

  public static AdminBookingDetailResponse from(AdminBookingDetailView booking) {
    return new AdminBookingDetailResponse(
        booking.id(),
        booking.publicReference(),
        new Guesthouse(booking.guesthouseId(), booking.guesthouseName()),
        new Stay(
            booking.checkInDate(),
            booking.checkOutDate(),
            ChronoUnit.DAYS.between(booking.checkInDate(), booking.checkOutDate()),
            booking.adults(),
            booking.childrenAge3to10(),
            booking.childrenAge0to3()),
        new Contact(
            booking.contactName(),
            booking.contactEmail(),
            booking.contactPhone(),
            booking.preferredLanguage()),
        new Services(booking.breakfastParticipants(), booking.dinnerParticipants()),
        booking.rooms().stream()
            .map(room -> new RoomSelection(room.roomTypeId(), room.roomTypeName(), room.quantity()))
            .toList(),
        new PriceSnapshot(
            booking.accommodationTotal(),
            booking.singleRoomSurcharge(),
            booking.breakfastTotal(),
            booking.dinnerTotal(),
            booking.totalPayable(),
            booking.currency()),
        booking.status(),
        booking.statusHistory().stream()
            .map(
                history ->
                    new StatusHistory(history.status(), history.changedAt(), history.changedBy()))
            .toList(),
        booking.guestNote(),
        booking.internalNote(),
        booking.createdAt(),
        booking.updatedAt());
  }

  public record Guesthouse(UUID id, String name) {}

  public record Stay(
      LocalDate checkInDate,
      LocalDate checkOutDate,
      long nights,
      int adults,
      int childrenAge3to10,
      int childrenAge0to3) {}

  public record Contact(String name, String email, String phone, String preferredLanguage) {}

  public record Services(int breakfastParticipants, int dinnerParticipants) {}

  public record RoomSelection(UUID roomTypeId, String roomTypeName, int quantity) {}

  public record PriceSnapshot(
      BigDecimal accommodationTotal,
      BigDecimal singleRoomSurcharge,
      BigDecimal breakfastTotal,
      BigDecimal dinnerTotal,
      BigDecimal totalPayable,
      String currency) {}

  public record StatusHistory(BookingStatus status, Instant changedAt, String changedBy) {}
}
