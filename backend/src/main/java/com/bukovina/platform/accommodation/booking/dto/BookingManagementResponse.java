package com.bukovina.platform.accommodation.booking.dto;

import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import com.bukovina.platform.accommodation.booking.service.BookingManagementView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record BookingManagementResponse(
    String reference,
    BookingStatus status,
    Guesthouse guesthouse,
    Stay stay,
    Guests guests,
    Services services,
    List<Room> rooms,
    Price price,
    boolean cancellationAllowed) {

  public static BookingManagementResponse from(BookingManagementView view, BookingStatus status) {
    return new BookingManagementResponse(
        view.publicReference(),
        status,
        new Guesthouse(
            view.guesthouseName(),
            view.contacts().stream()
                .map(
                    contact ->
                        new Contact(
                            contact.type(), contact.value(), contact.label(), contact.preferred()))
                .toList()),
        new Stay(
            view.checkInDate(),
            view.checkOutDate(),
            ChronoUnit.DAYS.between(view.checkInDate(), view.checkOutDate())),
        new Guests(view.adults(), view.childrenAge3to10(), view.childrenAge0to3()),
        new Services(view.breakfastParticipants(), view.dinnerParticipants()),
        view.rooms().stream().map(room -> new Room(room.name(), room.quantity())).toList(),
        new Price(
            view.accommodationTotal(),
            view.breakfastTotal(),
            view.dinnerTotal(),
            view.totalPayable(),
            view.currency()),
        status == BookingStatus.RECEIVED || status == BookingStatus.UNDER_REVIEW);
  }

  public record Guesthouse(String name, List<Contact> contacts) {}

  public record Contact(String type, String value, String label, boolean preferred) {}

  public record Stay(LocalDate checkInDate, LocalDate checkOutDate, long nights) {}

  public record Guests(int adults, int childrenAge3to10, int childrenAge0to3) {}

  public record Services(int breakfastParticipants, int dinnerParticipants) {}

  public record Room(String name, int quantity) {}

  public record Price(
      BigDecimal accommodationTotal,
      BigDecimal breakfastTotal,
      BigDecimal dinnerTotal,
      BigDecimal totalPayable,
      String currency) {}
}
