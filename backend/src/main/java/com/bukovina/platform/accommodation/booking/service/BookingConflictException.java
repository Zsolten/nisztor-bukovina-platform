package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.dto.BookingQuoteResponse;

public class BookingConflictException extends RuntimeException {

  private final String code;
  private final BookingQuoteResponse currentQuote;

  public BookingConflictException(String code, BookingQuoteResponse currentQuote) {
    super("Booking request conflict");
    this.code = code;
    this.currentQuote = currentQuote;
  }

  public String getCode() {
    return code;
  }

  public BookingQuoteResponse getCurrentQuote() {
    return currentQuote;
  }
}
