package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.dto.BookingPriceBreakdownResponse;
import com.bukovina.platform.accommodation.booking.dto.BookingPriceLineResponse;
import com.bukovina.platform.accommodation.booking.dto.BookingQuoteResponse;
import com.bukovina.platform.accommodation.pricing.service.PricingQuery;
import com.bukovina.platform.accommodation.pricing.service.PricingView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BookingPriceCalculator {

  private final PricingQuery pricingQuery;

  public BookingPriceCalculator(PricingQuery pricingQuery) {
    this.pricingQuery = pricingQuery;
  }

  public BookingQuoteResponse calculate(ValidatedBooking booking, String language) {
    PricingView pricing = pricingQuery.findPublished(booking.guesthouseId(), language);
    Map<String, PricingView.Item> items =
        pricing.items().stream()
            .collect(Collectors.toMap(PricingView.Item::id, Function.identity()));
    BigDecimal accommodationRate = requiredItem(items, "accommodation").amount();
    BigDecimal singleRate =
        booking.singleRoomCount() == 0
            ? accommodationRate
            : requiredItem(items, "single_occupancy_room").amount();
    long accommodationUnits = (long) booking.totalGuests() * booking.nights();
    long singleUnits = (long) booking.singleRoomCount() * booking.nights();
    BigDecimal accommodationTotal =
        money(accommodationRate.multiply(BigDecimal.valueOf(accommodationUnits)));
    BigDecimal singleUnitSurcharge = singleRate.subtract(accommodationRate).max(BigDecimal.ZERO);
    BigDecimal singleRoomSurcharge =
        money(singleUnitSurcharge.multiply(BigDecimal.valueOf(singleUnits)));
    BigDecimal breakfastTotal =
        serviceTotal(items, "breakfast", booking.breakfastParticipants(), booking.nights());
    BigDecimal dinnerTotal =
        serviceTotal(items, "dinner", booking.dinnerParticipants(), booking.nights());
    BigDecimal totalPayable =
        money(accommodationTotal.add(singleRoomSurcharge).add(breakfastTotal).add(dinnerTotal));

    List<BookingPriceLineResponse> lines = new ArrayList<>();
    lines.add(
        new BookingPriceLineResponse(
            "accommodation", accommodationUnits, money(accommodationRate), accommodationTotal));
    if (singleUnits > 0) {
      lines.add(
          new BookingPriceLineResponse(
              "single_room_surcharge",
              singleUnits,
              money(singleUnitSurcharge),
              singleRoomSurcharge));
    }
    addServiceLine(
        lines,
        items,
        "breakfast",
        booking.breakfastParticipants(),
        booking.nights(),
        breakfastTotal);
    addServiceLine(
        lines, items, "dinner", booking.dinnerParticipants(), booking.nights(), dinnerTotal);
    return new BookingQuoteResponse(
        pricing.currency(),
        booking.nights(),
        booking.totalGuests(),
        booking.selectedRoomCount(),
        booking.selectedCapacity(),
        List.copyOf(lines),
        new BookingPriceBreakdownResponse(
            accommodationTotal, singleRoomSurcharge, breakfastTotal, dinnerTotal, totalPayable),
        true);
  }

  private BigDecimal serviceTotal(
      Map<String, PricingView.Item> items, String code, int participants, long nights) {
    if (participants == 0) {
      return money(BigDecimal.ZERO);
    }
    PricingView.Item item = items.get(code);
    if (item == null || !"person".equals(item.unit())) {
      throw new BookingValidationException(
          "BOOKING_SERVICE_NOT_AVAILABLE", "services." + code + "Participants", "activeService");
    }
    return money(item.amount().multiply(BigDecimal.valueOf((long) participants * nights)));
  }

  private void addServiceLine(
      List<BookingPriceLineResponse> lines,
      Map<String, PricingView.Item> items,
      String code,
      int participants,
      long nights,
      BigDecimal total) {
    if (participants == 0) {
      return;
    }
    PricingView.Item item = items.get(code);
    lines.add(
        new BookingPriceLineResponse(
            code, (long) participants * nights, money(item.amount()), total));
  }

  private PricingView.Item requiredItem(Map<String, PricingView.Item> items, String code) {
    PricingView.Item item = items.get(code);
    if (item == null || !"person_night".equals(item.unit())) {
      throw new IllegalStateException("Required booking price is not configured");
    }
    return item;
  }

  private BigDecimal money(BigDecimal amount) {
    return amount.setScale(2, RoundingMode.HALF_UP);
  }
}
