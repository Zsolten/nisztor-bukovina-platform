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

  private static final String CHILDREN_UNDER_TEN_DISCOUNT = "children_under_10";
  private static final String SINGLE_ROOM = "single_room";
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
    BigDecimal singleRoomRate = requiredItem(items, SINGLE_ROOM, "room_night").amount();
    BigDecimal childAccommodationRate =
        booking.childrenAge3to10() == 0
            ? BigDecimal.ZERO
            : discountedAccommodationRate(pricing, accommodationRate);
    long adultAccommodationUnits =
        (long) (booking.adults() - booking.singleRoomCount()) * booking.nights();
    long childAccommodationUnits = (long) booking.childrenAge3to10() * booking.nights();
    long freeChildAccommodationUnits = (long) booking.childrenAge0to3() * booking.nights();
    long singleRoomUnits = (long) booking.singleRoomCount() * booking.nights();
    BigDecimal adultAccommodationTotal =
        money(accommodationRate.multiply(BigDecimal.valueOf(adultAccommodationUnits)));
    BigDecimal childAccommodationTotal =
        money(childAccommodationRate.multiply(BigDecimal.valueOf(childAccommodationUnits)));
    BigDecimal singleRoomTotal =
        money(singleRoomRate.multiply(BigDecimal.valueOf(singleRoomUnits)));
    BigDecimal accommodationTotal =
        money(adultAccommodationTotal.add(childAccommodationTotal).add(singleRoomTotal));
    BigDecimal singleRoomSurcharge = money(BigDecimal.ZERO);
    BigDecimal breakfastTotal =
        serviceTotal(items, "breakfast", booking.breakfastParticipants(), booking.nights());
    BigDecimal dinnerTotal =
        serviceTotal(items, "dinner", booking.dinnerParticipants(), booking.nights());
    BigDecimal totalPayable = money(accommodationTotal.add(breakfastTotal).add(dinnerTotal));

    List<BookingPriceLineResponse> lines = new ArrayList<>();
    if (adultAccommodationUnits > 0) {
      lines.add(
          new BookingPriceLineResponse(
              "accommodation",
              adultAccommodationUnits,
              money(accommodationRate),
              money(accommodationRate.multiply(BigDecimal.valueOf(adultAccommodationUnits)))));
    }
    if (singleRoomUnits > 0) {
      lines.add(
          new BookingPriceLineResponse(
              SINGLE_ROOM, singleRoomUnits, money(singleRoomRate), singleRoomTotal));
    }
    if (childAccommodationUnits > 0) {
      lines.add(
          new BookingPriceLineResponse(
              "children_under_10_accommodation",
              childAccommodationUnits,
              money(childAccommodationRate),
              money(childAccommodationRate.multiply(BigDecimal.valueOf(childAccommodationUnits)))));
    }
    if (freeChildAccommodationUnits > 0) {
      lines.add(
          new BookingPriceLineResponse(
              "children_under_3_accommodation",
              freeChildAccommodationUnits,
              money(BigDecimal.ZERO),
              money(BigDecimal.ZERO)));
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
            accommodationTotal,
            adultAccommodationTotal,
            childAccommodationTotal,
            singleRoomSurcharge,
            breakfastTotal,
            dinnerTotal,
            totalPayable),
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

  private BigDecimal discountedAccommodationRate(
      PricingView pricing, BigDecimal accommodationRate) {
    BigDecimal discountPercentage =
        pricing.discounts().stream()
            .filter(adjustment -> CHILDREN_UNDER_TEN_DISCOUNT.equals(adjustment.id()))
            .findFirst()
            .map(PricingView.Adjustment::percentage)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Required children-under-ten accommodation discount is not configured"));
    return accommodationRate.multiply(BigDecimal.ONE.subtract(discountPercentage.movePointLeft(2)));
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
    return requiredItem(items, code, "person_night");
  }

  private PricingView.Item requiredItem(
      Map<String, PricingView.Item> items, String code, String expectedUnit) {
    PricingView.Item item = items.get(code);
    if (item == null || !expectedUnit.equals(item.unit())) {
      throw new IllegalStateException("Required booking price is not configured");
    }
    return item;
  }

  private BigDecimal money(BigDecimal amount) {
    return amount.setScale(2, RoundingMode.HALF_UP);
  }
}
