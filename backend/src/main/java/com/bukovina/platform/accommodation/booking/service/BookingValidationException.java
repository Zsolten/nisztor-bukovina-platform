package com.bukovina.platform.accommodation.booking.service;

import java.util.List;

public class BookingValidationException extends RuntimeException {

  private final List<BookingProblem> problems;

  public BookingValidationException(List<BookingProblem> problems) {
    super("Booking request validation failed");
    this.problems = List.copyOf(problems);
  }

  public BookingValidationException(String code, String field, String rule) {
    this(List.of(new BookingProblem(code, field, rule)));
  }

  public List<BookingProblem> getProblems() {
    return problems;
  }
}
