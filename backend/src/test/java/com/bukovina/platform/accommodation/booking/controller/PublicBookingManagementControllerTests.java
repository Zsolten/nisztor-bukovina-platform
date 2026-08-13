package com.bukovina.platform.accommodation.booking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import(PostgreSqlTestContainerConfiguration.class)
@Transactional
class PublicBookingManagementControllerTests {

  private static final String TOKEN = "A".repeat(43);

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void returnsOnlyTheLocalizedSummaryForAValidToken() throws Exception {
    UUID bookingId = insertBooking("RECEIVED", Instant.now().plusSeconds(3600), null);
    insertRoomSelection(bookingId);

    mockMvc
        .perform(get("/api/booking-management/{token}", TOKEN).param("lang", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reference").value("NB-MANAGEMENT0001"))
        .andExpect(jsonPath("$.status").value("RECEIVED"))
        .andExpect(jsonPath("$.guesthouse.name").value("Bukovina Guesthouse"))
        .andExpect(jsonPath("$.guesthouse.contacts").isArray())
        .andExpect(jsonPath("$.stay.nights").value(2))
        .andExpect(jsonPath("$.guests.adults").value(2))
        .andExpect(jsonPath("$.rooms[0].name").value("Double room"))
        .andExpect(jsonPath("$.price.totalPayable").value(700.00))
        .andExpect(jsonPath("$.cancellationAllowed").value(true))
        .andExpect(jsonPath("$.contactEmail").doesNotExist())
        .andExpect(jsonPath("$.managementTokenHash").doesNotExist());
  }

  @Test
  void returnsTheSameGenericResponseForUnknownExpiredAndRevokedTokens() throws Exception {
    String expected = "{\"code\":\"BOOKING_MANAGEMENT_LINK_INVALID\"}";
    insertBooking("RECEIVED", Instant.now().minusSeconds(1), null);

    mockMvc
        .perform(get("/api/booking-management/{token}", TOKEN))
        .andExpect(status().isNotFound())
        .andExpect(content().json(expected));
    mockMvc
        .perform(get("/api/booking-management/{token}", "B".repeat(43)))
        .andExpect(status().isNotFound())
        .andExpect(content().json(expected));

    jdbcTemplate.update(
        "UPDATE booking_request SET management_token_expires_at = ?, management_token_revoked_at = ?",
        Timestamp.from(Instant.now().plusSeconds(3600)),
        Timestamp.from(Instant.now()));
    mockMvc
        .perform(get("/api/booking-management/{token}", TOKEN))
        .andExpect(status().isNotFound())
        .andExpect(content().json(expected));
  }

  @Test
  void cancelsAReceivedRequestOnceAndRevokesItsToken() throws Exception {
    UUID bookingId = insertBooking("RECEIVED", Instant.now().plusSeconds(3600), null);

    mockMvc
        .perform(post("/api/booking-management/{token}/cancellation", TOKEN))
        .andExpect(status().isNoContent());

    assertEquals(
        "CANCELLED",
        jdbcTemplate.queryForObject(
            "SELECT status FROM booking_request WHERE id = ?", String.class, bookingId));
    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM booking_status_history WHERE booking_request_id = ? AND status = 'CANCELLED' AND changed_by = 'GUEST_MANAGEMENT_LINK'",
            Integer.class,
            bookingId));
    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM booking_request WHERE id = ? AND management_token_revoked_at IS NOT NULL",
            Integer.class,
            bookingId));

    mockMvc
        .perform(post("/api/booking-management/{token}/cancellation", TOKEN))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("BOOKING_MANAGEMENT_LINK_INVALID"));
  }

  @Test
  void keepsConfirmedBookingsVisibleButRejectsSelfServiceCancellation() throws Exception {
    UUID bookingId = insertBooking("CONFIRMED", Instant.now().plusSeconds(3600), null);

    mockMvc
        .perform(get("/api/booking-management/{token}", TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"))
        .andExpect(jsonPath("$.cancellationAllowed").value(false))
        .andExpect(jsonPath("$.guesthouse.contacts").isNotEmpty());
    mockMvc
        .perform(post("/api/booking-management/{token}/cancellation", TOKEN))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("BOOKING_CANCELLATION_NOT_ALLOWED"));

    assertEquals(
        "CONFIRMED",
        jdbcTemplate.queryForObject(
            "SELECT status FROM booking_request WHERE id = ?", String.class, bookingId));
    assertEquals(
        0,
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM booking_status_history WHERE booking_request_id = ?",
            Integer.class,
            bookingId));
  }

  private UUID insertBooking(String status, Instant expiresAt, Instant revokedAt) {
    UUID id = UUID.randomUUID();
    jdbcTemplate.update(
        """
        INSERT INTO booking_request (
            id, guesthouse_id, public_reference, idempotency_key_hash, request_fingerprint,
            check_in_date, check_out_date, adults, children_age_3_to_10,
            children_age_0_to_3, breakfast_participants, dinner_participants,
            contact_name, contact_email, contact_phone, preferred_language, status,
            accommodation_total, adult_accommodation_total, child_accommodation_total,
            single_room_surcharge, breakfast_total, dinner_total, total_payable, currency,
            management_token_hash, management_token_expires_at, management_token_revoked_at,
            created_at, updated_at
        ) VALUES (?, ?, 'NB-MANAGEMENT0001', ?, ?, '2026-09-01', '2026-09-03',
                  2, 0, 0, 2, 0, 'Teszt Vendeg', 'guest@example.com', '+40123456789',
                  'hu', ?, 520, 520, 0, 0, 180, 0, 700, 'RON', ?, ?, ?, ?, ?)
        """,
        id,
        guesthouseId(),
        "idempotency-" + id,
        "fingerprint-" + id,
        status,
        sha256(TOKEN),
        Timestamp.from(expiresAt),
        revokedAt == null ? null : Timestamp.from(revokedAt),
        Timestamp.from(Instant.now()),
        Timestamp.from(Instant.now()));
    return id;
  }

  private void insertRoomSelection(UUID bookingId) {
    jdbcTemplate.update(
        "INSERT INTO booking_room_selection (id, booking_request_id, room_type_id, quantity) VALUES (?, ?, ?, 1)",
        UUID.randomUUID(),
        bookingId,
        roomTypeId());
  }

  private UUID guesthouseId() {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM guesthouse WHERE slug = 'bukovina-panzio'", UUID.class);
  }

  private UUID roomTypeId() {
    return jdbcTemplate.queryForObject(
        """
        SELECT room.id FROM room_type room
        JOIN guesthouse ON guesthouse.id = room.guesthouse_id
        WHERE guesthouse.slug = 'bukovina-panzio' AND room.code = 'double'
        """,
        UUID.class);
  }

  private String sha256(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
