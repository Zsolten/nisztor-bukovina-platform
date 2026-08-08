package com.bukovina.platform.accommodation.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.bukovina.platform.accommodation.booking.dao.BookingRequestRepository;
import com.bukovina.platform.accommodation.booking.model.BookingPriceBreakdown;
import com.bukovina.platform.accommodation.booking.model.BookingRequest;
import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@Import(PostgreSqlTestContainerConfiguration.class)
@Transactional
class BookingRequestPersistenceTests {

  @Autowired private BookingRequestRepository bookingRequestRepository;

  @Autowired private EntityManager entityManager;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void persistsBookingAggregateWithRoomSelectionPriceSnapshotAndStatusHistory() {
    UUID guesthouseId = idFor("guesthouse", "slug", "bukovina-panzio");
    UUID roomTypeId = roomTypeIdFor(guesthouseId, "double");
    Instant statusChangedAt = Instant.parse("2026-08-08T09:30:00Z");

    BookingRequest bookingRequest =
        new BookingRequest(
            guesthouseId,
            LocalDate.of(2026, 8, 21),
            LocalDate.of(2026, 8, 24),
            2,
            1,
            0,
            "Teszt Vendég",
            "guest@example.com",
            "+40 700 000 000",
            "hu",
            "Csendes szobát szeretnénk.",
            BookingStatus.RECEIVED,
            new BookingPriceBreakdown(
                new BigDecimal("1170.00"),
                new BigDecimal("11.00"),
                new BigDecimal("128.70"),
                new BigDecimal("0.00"),
                new BigDecimal("1.00"),
                new BigDecimal("12.99"),
                new BigDecimal("1311.69")),
            "a".repeat(64),
            Instant.parse("2026-09-07T09:30:00Z"));
    bookingRequest.addRoomSelection(roomTypeId, 1);
    bookingRequest.addStatusHistory(BookingStatus.RECEIVED, statusChangedAt, "SYSTEM");

    BookingRequest saved = bookingRequestRepository.saveAndFlush(bookingRequest);
    UUID bookingId = saved.getId();
    entityManager.clear();

    BookingRequest reloaded = bookingRequestRepository.findById(bookingId).orElseThrow();

    assertEquals(3, reloaded.getNights());
    assertEquals(guesthouseId, reloaded.getGuesthouseId());
    assertEquals(BookingStatus.RECEIVED, reloaded.getStatus());
    assertEquals(new BigDecimal("11.00"), reloaded.getPriceBreakdown().getAccommodationTaxRate());
    assertEquals(
        new BigDecimal("128.70"), reloaded.getPriceBreakdown().getAccommodationTaxAmount());
    assertEquals(new BigDecimal("1.00"), reloaded.getPriceBreakdown().getCityTaxRate());
    assertEquals(new BigDecimal("12.99"), reloaded.getPriceBreakdown().getCityTaxAmount());
    assertEquals(new BigDecimal("1311.69"), reloaded.getPriceBreakdown().getTotalPayable());
    assertEquals(1, reloaded.getRoomSelections().size());
    assertEquals(roomTypeId, reloaded.getRoomSelections().getFirst().getRoomTypeId());
    assertEquals(1, reloaded.getRoomSelections().getFirst().getQuantity());
    assertEquals(1, reloaded.getStatusHistory().size());
    assertEquals(statusChangedAt, reloaded.getStatusHistory().getFirst().getChangedAt());
    assertEquals("SYSTEM", reloaded.getStatusHistory().getFirst().getChangedBy());
    assertNotNull(reloaded.getCreatedAt());
    assertNotNull(reloaded.getUpdatedAt());
  }

  private UUID idFor(String tableName, String columnName, String value) {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM " + tableName + " WHERE " + columnName + " = ?", UUID.class, value);
  }

  private UUID roomTypeIdFor(UUID guesthouseId, String code) {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM room_type WHERE guesthouse_id = ? AND code = ?",
        UUID.class,
        guesthouseId,
        code);
  }
}
