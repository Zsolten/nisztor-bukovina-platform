package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.dto.BookingQuoteRequest;
import com.bukovina.platform.accommodation.booking.dto.BookingQuoteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingQuoteService {

  private final BookingValidator validator;
  private final BookingPriceCalculator calculator;

  public BookingQuoteService(BookingValidator validator, BookingPriceCalculator calculator) {
    this.validator = validator;
    this.calculator = calculator;
  }

  @Transactional(readOnly = true)
  public BookingQuoteResponse quote(BookingQuoteRequest request) {
    if (request == null) {
      throw new BookingValidationException("REQUEST_REQUIRED", "request", "required");
    }
    return calculator.calculate(validator.validate(request), "hu");
  }
}
