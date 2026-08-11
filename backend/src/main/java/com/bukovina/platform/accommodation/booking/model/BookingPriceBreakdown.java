package com.bukovina.platform.accommodation.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class BookingPriceBreakdown {

  @Column(name = "accommodation_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal accommodationTotal;

  @Column(name = "adult_accommodation_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal adultAccommodationTotal;

  @Column(name = "child_accommodation_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal childAccommodationTotal;

  @Column(name = "single_room_surcharge", nullable = false, precision = 12, scale = 2)
  private BigDecimal singleRoomSurcharge;

  @Column(name = "breakfast_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal breakfastTotal;

  @Column(name = "dinner_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal dinnerTotal;

  @Column(name = "total_payable", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalPayable;

  protected BookingPriceBreakdown() {}

  public BookingPriceBreakdown(
      BigDecimal accommodationTotal,
      BigDecimal adultAccommodationTotal,
      BigDecimal childAccommodationTotal,
      BigDecimal singleRoomSurcharge,
      BigDecimal breakfastTotal,
      BigDecimal dinnerTotal,
      BigDecimal totalPayable) {
    this.accommodationTotal = Objects.requireNonNull(accommodationTotal);
    this.adultAccommodationTotal = Objects.requireNonNull(adultAccommodationTotal);
    this.childAccommodationTotal = Objects.requireNonNull(childAccommodationTotal);
    this.singleRoomSurcharge = Objects.requireNonNull(singleRoomSurcharge);
    this.breakfastTotal = Objects.requireNonNull(breakfastTotal);
    this.dinnerTotal = Objects.requireNonNull(dinnerTotal);
    this.totalPayable = Objects.requireNonNull(totalPayable);
  }

  public BigDecimal getAccommodationTotal() {
    return accommodationTotal;
  }

  public BigDecimal getSingleRoomSurcharge() {
    return singleRoomSurcharge;
  }

  public BigDecimal getAdultAccommodationTotal() {
    return adultAccommodationTotal;
  }

  public BigDecimal getChildAccommodationTotal() {
    return childAccommodationTotal;
  }

  public BigDecimal getBreakfastTotal() {
    return breakfastTotal;
  }

  public BigDecimal getDinnerTotal() {
    return dinnerTotal;
  }

  public BigDecimal getTotalPayable() {
    return totalPayable;
  }
}
