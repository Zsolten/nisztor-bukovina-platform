package com.bukovina.platform.support.notification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record NotificationBookingView(
    UUID id,
    String publicReference,
    String guesthouseName,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    long nights,
    int adults,
    int childrenAge3to10,
    int childrenAge0to3,
    int breakfastParticipants,
    int dinnerParticipants,
    BigDecimal accommodationTotal,
    BigDecimal singleRoomSurcharge,
    BigDecimal breakfastTotal,
    BigDecimal dinnerTotal,
    BigDecimal totalPayable,
    String currency,
    List<NotificationRoomView> rooms) {

  public int totalGuests() {
    return adults + childrenAge3to10 + childrenAge0to3;
  }
}
