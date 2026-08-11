package com.bukovina.platform.accommodation.booking.model;

public class InvalidBookingStatusTransitionException extends IllegalStateException {

  private final BookingStatus currentStatus;
  private final BookingStatus requestedStatus;

  public InvalidBookingStatusTransitionException(
      BookingStatus currentStatus, BookingStatus requestedStatus) {
    super("Invalid booking status transition");
    this.currentStatus = currentStatus;
    this.requestedStatus = requestedStatus;
  }

  public BookingStatus getCurrentStatus() {
    return currentStatus;
  }

  public BookingStatus getRequestedStatus() {
    return requestedStatus;
  }
}
