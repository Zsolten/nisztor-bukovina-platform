package com.bukovina.platform.accommodation.booking.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import(PostgreSqlTestContainerConfiguration.class)
@Transactional
class AdminBookingControllerTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void rejectsAnonymousListAndDetailRequests() throws Exception {
    mockMvc.perform(get("/api/admin/bookings")).andExpect(status().isUnauthorized());
    mockMvc
        .perform(get("/api/admin/bookings/{bookingId}", UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USER")
  void rejectsAuthenticatedUsersWithoutTheAdministratorRole() throws Exception {
    mockMvc.perform(get("/api/admin/bookings")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void filtersPaginatesAndSortsBookingsNewestFirst() throws Exception {
    UUID bukovinaId = guesthouseId("bukovina-panzio");
    UUID nisztorId = guesthouseId("nisztor-panzio");
    UUID oldest =
        insertBooking(
            bukovinaId, "NB-0000000000000001", "RECEIVED", Instant.parse("2026-08-09T08:00:00Z"));
    UUID middle =
        insertBooking(
            bukovinaId, "NB-0000000000000002", "CONFIRMED", Instant.parse("2026-08-10T08:00:00Z"));
    UUID newest =
        insertBooking(
            bukovinaId, "NB-0000000000000003", "RECEIVED", Instant.parse("2026-08-11T08:00:00Z"));
    UUID otherGuesthouse =
        insertBooking(
            nisztorId, "NB-0000000000000004", "RECEIVED", Instant.parse("2026-08-08T08:00:00Z"));

    mockMvc
        .perform(get("/api/admin/bookings").param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(newest.toString()))
        .andExpect(jsonPath("$.content[1].id").value(middle.toString()))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").value(4))
        .andExpect(jsonPath("$.totalPages").value(2));

    mockMvc
        .perform(get("/api/admin/bookings").param("page", "1").param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(oldest.toString()))
        .andExpect(jsonPath("$.content[1].id").value(otherGuesthouse.toString()));

    mockMvc
        .perform(
            get("/api/admin/bookings")
                .param("guesthouseId", bukovinaId.toString())
                .param("status", "RECEIVED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content[0].id").value(newest.toString()))
        .andExpect(jsonPath("$.content[1].id").value(oldest.toString()));

    mockMvc
        .perform(
            get("/api/admin/bookings")
                .param("createdFrom", "2026-08-10")
                .param("createdTo", "2026-08-11"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content[0].id").value(newest.toString()))
        .andExpect(jsonPath("$.content[1].id").value(middle.toString()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void returnsCompleteBookingDetailWithoutSensitivePersistenceFields() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID bookingId =
        insertBooking(
            guesthouseId, "NB-0000000000000010", "RECEIVED", Instant.parse("2026-08-11T08:00:00Z"));
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    jdbcTemplate.update(
        """
        INSERT INTO booking_room_selection (id, booking_request_id, room_type_id, quantity)
        VALUES (?, ?, ?, 2)
        """,
        UUID.randomUUID(),
        bookingId,
        roomTypeId);
    jdbcTemplate.update(
        """
        INSERT INTO booking_status_history (id, booking_request_id, status, changed_at, changed_by)
        VALUES (?, ?, 'RECEIVED', ?, 'SYSTEM')
        """,
        UUID.randomUUID(),
        bookingId,
        Timestamp.from(Instant.parse("2026-08-11T08:00:00Z")));
    jdbcTemplate.update(
        "UPDATE booking_request SET internal_note = ? WHERE id = ?",
        "Visszahívás szükséges",
        bookingId);

    MvcResult result =
        mockMvc
            .perform(get("/api/admin/bookings/{bookingId}", bookingId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(bookingId.toString()))
            .andExpect(jsonPath("$.guesthouse.name").value("Bukovina Panzió"))
            .andExpect(jsonPath("$.stay.nights").value(2))
            .andExpect(jsonPath("$.stay.adults").value(2))
            .andExpect(jsonPath("$.contact.email").value("guest@example.com"))
            .andExpect(jsonPath("$.services.breakfastParticipants").value(2))
            .andExpect(jsonPath("$.rooms[0].roomTypeId").value(roomTypeId.toString()))
            .andExpect(jsonPath("$.rooms[0].roomTypeName").value("Kétágyas szoba"))
            .andExpect(jsonPath("$.priceSnapshot.totalPayable").value(700.00))
            .andExpect(jsonPath("$.statusHistory[0].status").value("RECEIVED"))
            .andExpect(jsonPath("$.guestNote").value("Csendes szobát kérünk"))
            .andExpect(jsonPath("$.internalNote").value("Visszahívás szükséges"))
            .andExpect(jsonPath("$.managementToken").doesNotExist())
            .andExpect(jsonPath("$.managementTokenHash").doesNotExist())
            .andExpect(jsonPath("$.idempotencyKeyHash").doesNotExist())
            .andExpect(jsonPath("$.requestFingerprint").doesNotExist())
            .andReturn();

    String body = result.getResponse().getContentAsString();
    assertFalse(body.contains("management-token-secret"));
    assertFalse(body.contains("idempotency-secret"));
    assertFalse(body.contains("fingerprint-secret"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void keepsBookingsAndRoomSelectionsVisibleWhenHungarianNamesAreMissing() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    UUID bookingId =
        insertBooking(
            guesthouseId, "NB-0000000000000011", "RECEIVED", Instant.parse("2026-08-11T08:00:00Z"));
    jdbcTemplate.update(
        """
        INSERT INTO booking_room_selection (id, booking_request_id, room_type_id, quantity)
        VALUES (?, ?, ?, 1)
        """,
        UUID.randomUUID(),
        bookingId,
        roomTypeId);
    jdbcTemplate.update(
        "DELETE FROM guesthouse_translation WHERE guesthouse_id = ? AND language_code = 'hu'",
        guesthouseId);
    jdbcTemplate.update(
        "DELETE FROM room_type_translation WHERE room_type_id = ? AND language_code = 'hu'",
        roomTypeId);

    mockMvc
        .perform(get("/api/admin/bookings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(bookingId.toString()))
        .andExpect(jsonPath("$.content[0].guesthouseName").value("bukovina-panzio"));
    mockMvc
        .perform(get("/api/admin/bookings/{bookingId}", bookingId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.guesthouse.name").value("bukovina-panzio"))
        .andExpect(jsonPath("$.rooms[0].roomTypeId").value(roomTypeId.toString()))
        .andExpect(jsonPath("$.rooms[0].roomTypeName").value("double"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void returnsNotFoundAndRejectsInvalidPaginationOrDateRange() throws Exception {
    mockMvc
        .perform(get("/api/admin/bookings/{bookingId}", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ADMIN_BOOKING_NOT_FOUND"));
    mockMvc
        .perform(get("/api/admin/bookings").param("page", "-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_ADMIN_BOOKING_QUERY"));
    mockMvc
        .perform(get("/api/admin/bookings").param("size", "101"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_ADMIN_BOOKING_QUERY"));
    mockMvc
        .perform(
            get("/api/admin/bookings")
                .param("createdFrom", "2026-08-12")
                .param("createdTo", "2026-08-11"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_ADMIN_BOOKING_QUERY"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void returnsTheDocumentedErrorForMalformedQueryParameters() throws Exception {
    assertInvalidQuery("guesthouseId", "not-a-uuid");
    assertInvalidQuery("status", "UNKNOWN");
    assertInvalidQuery("createdFrom", "not-a-date");
    assertInvalidQuery("page", "not-a-number");
  }

  private UUID insertBooking(
      UUID guesthouseId, String publicReference, String status, Instant createdAt) {
    UUID id = UUID.randomUUID();
    jdbcTemplate.update(
        """
        INSERT INTO booking_request (
            id, guesthouse_id, public_reference, idempotency_key_hash, request_fingerprint,
            check_in_date, check_out_date, adults, children_age_3_to_10,
            children_age_0_to_3, breakfast_participants, dinner_participants,
            contact_name, contact_email, contact_phone, preferred_language, note, status,
            accommodation_total, single_room_surcharge, breakfast_total, dinner_total,
            total_payable, currency, management_token_hash, management_token_expires_at,
            created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, 2, 1, 0, 2, 0, ?, ?, ?, 'hu', ?, ?,
                  ?, ?, ?, ?, ?, 'RON', ?, ?, ?, ?)
        """,
        id,
        guesthouseId,
        publicReference,
        "idempotency-secret-" + id,
        "fingerprint-secret-" + id,
        LocalDate.of(2026, 9, 1),
        LocalDate.of(2026, 9, 3),
        "Teszt Vendég",
        "guest@example.com",
        "+40123456789",
        "Csendes szobát kérünk",
        status,
        new BigDecimal("500.00"),
        new BigDecimal("20.00"),
        new BigDecimal("180.00"),
        BigDecimal.ZERO,
        new BigDecimal("700.00"),
        "management-token-secret-" + id,
        Timestamp.from(createdAt.plus(30, ChronoUnit.DAYS)),
        Timestamp.from(createdAt),
        Timestamp.from(createdAt));
    return id;
  }

  private void assertInvalidQuery(String parameter, String value) throws Exception {
    mockMvc
        .perform(get("/api/admin/bookings").param(parameter, value))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_ADMIN_BOOKING_QUERY"));
  }

  private UUID guesthouseId(String slug) {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM guesthouse WHERE slug = ?", UUID.class, slug);
  }

  private UUID roomTypeId(String guesthouseSlug, String code) {
    return jdbcTemplate.queryForObject(
        """
        SELECT room.id
        FROM room_type room
        JOIN guesthouse ON guesthouse.id = room.guesthouse_id
        WHERE guesthouse.slug = ? AND room.code = ?
        """,
        UUID.class,
        guesthouseSlug,
        code);
  }
}
