package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.model.BookingRequest;
import com.bukovina.platform.accommodation.booking.model.BookingStatus;

public interface BookingNotificationOutbox {

  void enqueueBookingReceived(BookingRequest booking, String rawManagementToken);

  void enqueueBookingDecision(BookingRequest booking, BookingStatus decision, String guestMessage);
}
