package com.bukovina.platform.accommodation.booking.service;

import java.util.UUID;

public class AdminBookingNotFoundException extends RuntimeException {

  public AdminBookingNotFoundException(UUID bookingId) {
    super("Booking request not found: " + bookingId);
  }
}
