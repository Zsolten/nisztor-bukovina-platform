package com.bukovina.platform.accommodation.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bukovina.platform.accommodation.booking.dto.BookingQuoteResponse;
import com.bukovina.platform.accommodation.pricing.service.PricingQuery;
import com.bukovina.platform.accommodation.pricing.service.PricingView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BookingPriceCalculatorTests {

  private static final UUID GUESTHOUSE_ID = UUID.randomUUID();
  private final BookingPriceCalculator calculator = new BookingPriceCalculator(pricing());

  @Test
  void calculatesTotalFromAccommodationAndSelectedMealsOnly() {
    BookingQuoteResponse quote = calculator.calculate(booking(7, 0, 7, 7), "hu");

    assertEquals(new BigDecimal("2730.00"), quote.priceBreakdown().accommodationTotal());
    assertEquals(new BigDecimal("945.00"), quote.priceBreakdown().breakfastTotal());
    assertEquals(new BigDecimal("1575.00"), quote.priceBreakdown().dinnerTotal());
    assertEquals(new BigDecimal("5250.00"), quote.priceBreakdown().totalPayable());
  }

  @Test
  void representsSingleRoomPriceAsBaseAccommodationPlusSurcharge() {
    BookingQuoteResponse quote = calculator.calculate(booking(5, 1, 0, 0), "hu");

    assertEquals(new BigDecimal("1950.00"), quote.priceBreakdown().accommodationTotal());
    assertEquals(new BigDecimal("210.00"), quote.priceBreakdown().singleRoomSurcharge());
    assertEquals(new BigDecimal("2160.00"), quote.priceBreakdown().totalPayable());
  }

  @Test
  void chargesMealsOnlyForTheSelectedParticipantCount() {
    BookingQuoteResponse quote = calculator.calculate(booking(5, 0, 2, 3), "hu");

    assertEquals(new BigDecimal("270.00"), quote.priceBreakdown().breakfastTotal());
    assertEquals(new BigDecimal("675.00"), quote.priceBreakdown().dinnerTotal());
  }

  private ValidatedBooking booking(
      int guests, int singleRooms, int breakfastParticipants, int dinnerParticipants) {
    return new ValidatedBooking(
        GUESTHOUSE_ID,
        LocalDate.of(2030, 1, 10),
        LocalDate.of(2030, 1, 13),
        3,
        guests,
        0,
        0,
        guests,
        breakfastParticipants,
        dinnerParticipants,
        3,
        guests,
        singleRooms,
        List.of());
  }

  private PricingQuery pricing() {
    return (guesthouseId, language) ->
        new PricingView(
            "RON",
            List.of(
                new PricingView.Item(
                    "accommodation", "Accommodation", new BigDecimal("130"), "person_night"),
                new PricingView.Item(
                    "single_occupancy_room", "Single", new BigDecimal("200"), "person_night"),
                new PricingView.Item("breakfast", "Breakfast", new BigDecimal("45"), "person"),
                new PricingView.Item("dinner", "Dinner", new BigDecimal("75"), "person")),
            List.of(new PricingView.Adjustment("city_tax", "City tax", new BigDecimal("99"))),
            List.of(),
            List.of(),
            "Payment on arrival");
  }
}
