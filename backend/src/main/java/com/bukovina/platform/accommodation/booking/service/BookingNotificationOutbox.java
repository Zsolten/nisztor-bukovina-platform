package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.model.BookingRequest;

public interface BookingNotificationOutbox {

  void enqueueBookingReceived(BookingRequest booking, String rawManagementToken);
}
