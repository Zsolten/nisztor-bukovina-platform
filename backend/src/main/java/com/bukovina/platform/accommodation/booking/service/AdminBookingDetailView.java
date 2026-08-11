package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AdminBookingDetailView(
    UUID id,
    String publicReference,
    UUID guesthouseId,
    String guesthouseName,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    int adults,
    int childrenAge3to10,
    int childrenAge0to3,
    int breakfastParticipants,
    int dinnerParticipants,
    String contactName,
    String contactEmail,
    String contactPhone,
    String preferredLanguage,
    String guestNote,
    String internalNote,
    BookingStatus status,
    BigDecimal accommodationTotal,
    BigDecimal singleRoomSurcharge,
    BigDecimal breakfastTotal,
    BigDecimal dinnerTotal,
    BigDecimal totalPayable,
    String currency,
    Instant createdAt,
    Instant updatedAt,
    List<AdminBookingRoomView> rooms,
    List<AdminBookingStatusHistoryView> statusHistory) {}
