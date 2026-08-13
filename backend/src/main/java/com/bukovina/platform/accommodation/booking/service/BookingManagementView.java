package com.bukovina.platform.accommodation.booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BookingManagementView(
    String publicReference,
    String guesthouseName,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    int adults,
    int childrenAge3to10,
    int childrenAge0to3,
    int breakfastParticipants,
    int dinnerParticipants,
    BigDecimal accommodationTotal,
    BigDecimal breakfastTotal,
    BigDecimal dinnerTotal,
    BigDecimal totalPayable,
    String currency,
    List<Room> rooms,
    List<Contact> contacts) {

  public record Room(String name, int quantity) {}

  public record Contact(String type, String value, String label, boolean preferred) {}
}
