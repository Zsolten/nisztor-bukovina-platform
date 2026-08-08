package com.bukovina.platform.accommodation.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class BookingPriceBreakdown {

  @Column(name = "net_accommodation", nullable = false, precision = 12, scale = 2)
  private BigDecimal netAccommodation;

  @Column(name = "accommodation_tax_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal accommodationTaxRate;

  @Column(name = "accommodation_tax_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal accommodationTaxAmount;

  @Column(name = "single_room_surcharge", nullable = false, precision = 12, scale = 2)
  private BigDecimal singleRoomSurcharge;

  @Column(name = "city_tax_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal cityTaxRate;

  @Column(name = "city_tax_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal cityTaxAmount;

  @Column(name = "total_payable", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalPayable;

  protected BookingPriceBreakdown() {}

  public BookingPriceBreakdown(
      BigDecimal netAccommodation,
      BigDecimal accommodationTaxRate,
      BigDecimal accommodationTaxAmount,
      BigDecimal singleRoomSurcharge,
      BigDecimal cityTaxRate,
      BigDecimal cityTaxAmount,
      BigDecimal totalPayable) {
    this.netAccommodation = Objects.requireNonNull(netAccommodation);
    this.accommodationTaxRate = Objects.requireNonNull(accommodationTaxRate);
    this.accommodationTaxAmount = Objects.requireNonNull(accommodationTaxAmount);
    this.singleRoomSurcharge = Objects.requireNonNull(singleRoomSurcharge);
    this.cityTaxRate = Objects.requireNonNull(cityTaxRate);
    this.cityTaxAmount = Objects.requireNonNull(cityTaxAmount);
    this.totalPayable = Objects.requireNonNull(totalPayable);
  }

  public BigDecimal getNetAccommodation() {
    return netAccommodation;
  }

  public BigDecimal getAccommodationTaxRate() {
    return accommodationTaxRate;
  }

  public BigDecimal getAccommodationTaxAmount() {
    return accommodationTaxAmount;
  }

  public BigDecimal getSingleRoomSurcharge() {
    return singleRoomSurcharge;
  }

  public BigDecimal getCityTaxRate() {
    return cityTaxRate;
  }

  public BigDecimal getCityTaxAmount() {
    return cityTaxAmount;
  }

  public BigDecimal getTotalPayable() {
    return totalPayable;
  }
}
