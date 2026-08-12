package com.bukovina.platform.accommodation.booking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
      "DB_PASSWORD=test-password",
      "booking.notification.enabled=true",
      "booking.notification.token-encryption-key=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
      "booking.notification.from-address=sender@example.com",
      "booking.notification.worker-delay=PT1H"
    })
@AutoConfigureMockMvc
@Import(PostgreSqlTestContainerConfiguration.class)
@Transactional
class PublicBookingControllerTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void allowsAnonymousQuoteAndUsesAuthoritativePricesWithoutPersisting() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");

    mockMvc
        .perform(
            post("/api/booking-quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(quoteJson(guesthouseId, roomTypeId, 2, 0, 0, 1, 2, 0)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currency").value("RON"))
        .andExpect(jsonPath("$.nights").value(2))
        .andExpect(jsonPath("$.totalGuests").value(2))
        .andExpect(jsonPath("$.selectedRoomCount").value(1))
        .andExpect(jsonPath("$.selectedCapacity").value(2))
        .andExpect(jsonPath("$.priceBreakdown.accommodationTotal").value(520.00))
        .andExpect(jsonPath("$.priceBreakdown.adultAccommodationTotal").value(520.00))
        .andExpect(jsonPath("$.priceBreakdown.childAccommodationTotal").value(0.00))
        .andExpect(jsonPath("$.priceBreakdown.accommodationTaxRate").doesNotExist())
        .andExpect(jsonPath("$.priceBreakdown.cityTaxAmount").doesNotExist())
        .andExpect(jsonPath("$.priceBreakdown.breakfastTotal").value(180.00))
        .andExpect(jsonPath("$.priceBreakdown.totalPayable").value(700.00))
        .andExpect(jsonPath("$.requestOnly").value(true));

    assertEquals(0, count("booking_request"));
  }

  @Test
  void appliesChildAccommodationDiscountsToThePublicQuote() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");

    mockMvc
        .perform(
            post("/api/booking-quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(quoteJson(guesthouseId, roomTypeId, 2, 1, 1, 2, 0, 0)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.priceBreakdown.accommodationTotal").value(715.00))
        .andExpect(jsonPath("$.priceBreakdown.adultAccommodationTotal").value(520.00))
        .andExpect(jsonPath("$.priceBreakdown.childAccommodationTotal").value(195.00))
        .andExpect(jsonPath("$.priceBreakdown.totalPayable").value(715.00))
        .andExpect(jsonPath("$.lines[1].code").value("children_under_10_accommodation"))
        .andExpect(jsonPath("$.lines[1].unitAmount").value(97.50))
        .andExpect(jsonPath("$.lines[2].code").value("children_under_3_accommodation"))
        .andExpect(jsonPath("$.lines[2].lineTotal").value(0.00));
  }

  @Test
  void rejectsInvalidInactiveAndMismatchedGuesthouseOrRoomTypes() throws Exception {
    UUID bukovinaId = guesthouseId("bukovina-panzio");
    UUID nisztorRoomId = roomTypeId("nisztor-panzio", "double");

    expectError(
        quoteJson(UUID.randomUUID(), nisztorRoomId, 2, 0, 0, 1, 0, 0), "GUESTHOUSE_NOT_AVAILABLE");
    expectError(quoteJson(bukovinaId, UUID.randomUUID(), 2, 0, 0, 1, 0, 0), "ROOM_TYPE_NOT_FOUND");
    expectError(
        quoteJson(bukovinaId, nisztorRoomId, 2, 0, 0, 1, 0, 0), "ROOM_TYPE_GUESTHOUSE_MISMATCH");

    UUID bukovinaRoomId = roomTypeId("bukovina-panzio", "double");
    jdbcTemplate.update("UPDATE room_type SET active = FALSE WHERE id = ?", bukovinaRoomId);
    expectError(quoteJson(bukovinaId, bukovinaRoomId, 2, 0, 0, 1, 0, 0), "ROOM_TYPE_NOT_BOOKABLE");

    jdbcTemplate.update("UPDATE guesthouse SET active = FALSE WHERE id = ?", bukovinaId);
    expectError(
        quoteJson(bukovinaId, bukovinaRoomId, 2, 0, 0, 1, 0, 0), "GUESTHOUSE_NOT_AVAILABLE");
  }

  @Test
  void validatesPastAndNonIncreasingCalendarDates() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    String valid = quoteJson(guesthouseId, roomTypeId, 2, 0, 0, 1, 0, 0);

    expectError(
        valid.replace(futureCheckIn().toString(), LocalDate.now().minusDays(1).toString()),
        "CHECK_IN_IN_PAST");
    expectError(
        valid.replace(futureCheckOut().toString(), futureCheckIn().toString()),
        "INVALID_DATE_RANGE");
  }

  @Test
  void validatesGuestCountsAndLargeGroupBoundary() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");

    expectError(quoteJson(guesthouseId, roomTypeId, 0, 0, 0, 1, 0, 0), "TOTAL_GUESTS_REQUIRED");
    expectError(quoteJson(guesthouseId, roomTypeId, -1, 0, 0, 1, 0, 0), "NEGATIVE_GUEST_COUNT");
    expectError(quoteJson(guesthouseId, roomTypeId, 20, 0, 0, 6, 0, 0), "LARGE_GROUP_OFFLINE_ONLY");

    UUID triple = roomTypeId("bukovina-panzio", "triple");
    UUID quadruple = roomTypeId("bukovina-panzio", "quadruple");
    String boundaryRequest =
        quoteJson(guesthouseId, roomTypeId, 19, 0, 0, 6, 0, 0)
            .replace(
                "{\"roomTypeId\": \"" + roomTypeId + "\", \"quantity\": 6}",
                "{\"roomTypeId\": \""
                    + roomTypeId
                    + "\", \"quantity\": 6},"
                    + "{\"roomTypeId\": \""
                    + triple
                    + "\", \"quantity\": 1},"
                    + "{\"roomTypeId\": \""
                    + quadruple
                    + "\", \"quantity\": 1}");
    mockMvc
        .perform(
            post("/api/booking-quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(boundaryRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalGuests").value(19));
  }

  @Test
  void validatesRoomQuantityStockCapacityAndGuestPerRoomRules() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID doubleRoom = roomTypeId("bukovina-panzio", "double");

    expectError(quoteJson(guesthouseId, doubleRoom, 2, 0, 0, 0, 0, 0), "INVALID_ROOM_QUANTITY");
    expectError(quoteJson(guesthouseId, doubleRoom, 2, 0, 0, -1, 0, 0), "INVALID_ROOM_QUANTITY");
    expectError(
        quoteJson(guesthouseId, doubleRoom, 2, 0, 0, 7, 0, 0), "ROOM_QUANTITY_EXCEEDS_STOCK");
    expectError(
        quoteJson(guesthouseId, doubleRoom, 3, 0, 0, 1, 0, 0), "INSUFFICIENT_ROOM_CAPACITY");
    expectError(quoteJson(guesthouseId, doubleRoom, 1, 0, 0, 2, 0, 0), "TOO_MANY_ROOMS");
  }

  @Test
  void validatesServiceParticipantsAndRejectsInactiveServices() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");

    expectError(
        quoteJson(guesthouseId, roomTypeId, 2, 0, 0, 1, -1, 0), "NEGATIVE_SERVICE_PARTICIPANTS");
    expectError(
        quoteJson(guesthouseId, roomTypeId, 2, 0, 0, 1, 3, 0),
        "SERVICE_PARTICIPANTS_EXCEED_GUESTS");

    setPriceActive("bukovina-panzio", "breakfast", false);
    expectError(
        quoteJson(guesthouseId, roomTypeId, 2, 0, 0, 1, 2, 0), "BOOKING_SERVICE_NOT_AVAILABLE");
  }

  @Test
  void rejectsUnsupportedInjectedServicesAndMalformedTypes() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    String request = quoteJson(guesthouseId, roomTypeId, 2, 0, 0, 1, 0, 0);

    mockMvc
        .perform(
            post("/api/booking-quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.replace("\"dinnerParticipants\": 0", "\"lunchParticipants\": 2")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    mockMvc
        .perform(
            post("/api/booking-quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.replace("\"adults\": 2", "\"adults\": 2.5")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }

  @Test
  void rejectsNullJsonBodiesWithoutLeakingAnInternalErrorOrPersisting() throws Exception {
    mockMvc
        .perform(
            post("/api/booking-quotes").contentType(MediaType.APPLICATION_JSON).content("null"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

    mockMvc
        .perform(
            post("/api/booking-requests")
                .header("Idempotency-Key", "null-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

    assertEquals(0, count("booking_request"));
  }

  @Test
  void rejectsOversizedOrOverflowingPublicInputBeforeItCanBePricedOrPersisted() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    String roomSelection = "{\"roomTypeId\": \"" + roomTypeId + "\", \"quantity\": 1}";
    String tooManyRooms =
        """
        {
          "guesthouseId": "%s",
          "checkInDate": "%s",
          "checkOutDate": "%s",
          "adults": 2,
          "childrenAge3to10": 0,
          "childrenAge0to3": 0,
          "roomSelections": [%s],
          "services": {"breakfastParticipants": 0, "dinnerParticipants": 0}
        }
        """
            .formatted(
                guesthouseId,
                futureCheckIn(),
                futureCheckOut(),
                String.join(",", java.util.Collections.nCopies(21, roomSelection)));

    expectError(tooManyRooms, "TOO_MANY_ROOM_SELECTIONS");
    expectError(
        quoteJson(guesthouseId, roomTypeId, 2, 0, 0, 1, 0, 0)
            .replace(
                "\"roomSelections\": [{\"roomTypeId\": \"" + roomTypeId + "\", \"quantity\": 1}]",
                "\"roomSelections\": [{\"roomTypeId\": \""
                    + roomTypeId
                    + "\", \"quantity\": 1},{\"roomTypeId\": \""
                    + roomTypeId
                    + "\", \"quantity\": 1}]"),
        "DUPLICATE_ROOM_TYPE");
    expectError(
        quoteJson(guesthouseId, roomTypeId, Integer.MAX_VALUE, Integer.MAX_VALUE, 1, 1, 0, 0),
        "GUEST_COUNT_TOO_LARGE");

    assertEquals(0, count("booking_request"));
  }

  @Test
  void validatesAndNormalizesAllContactFields() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    String invalid =
        submitJson(
            guesthouseId,
            roomTypeId,
            "   ",
            "not-an-email",
            "12",
            "de",
            "x".repeat(2001),
            "700.00");

    mockMvc
        .perform(
            post("/api/booking-requests")
                .header("Idempotency-Key", "invalid-contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalid))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.code == 'CONTACT_FIELD_REQUIRED')]").isNotEmpty())
        .andExpect(jsonPath("$.errors[?(@.code == 'INVALID_EMAIL')]").isNotEmpty())
        .andExpect(jsonPath("$.errors[?(@.code == 'INVALID_PHONE')]").isNotEmpty())
        .andExpect(jsonPath("$.errors[?(@.code == 'UNSUPPORTED_LANGUAGE')]").isNotEmpty())
        .andExpect(jsonPath("$.errors[?(@.code == 'TEXT_TOO_LONG')]").isNotEmpty());
    assertEquals(0, count("booking_request"));
  }

  @Test
  void rejectsInvalidMoneyPrecisionAndOversizedIdempotencyKeysWithoutPersistence()
      throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");

    mockMvc
        .perform(
            post("/api/booking-requests")
                .header("Idempotency-Key", "money-precision")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validSubmitJson(guesthouseId, roomTypeId, "700.001")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.code == 'INVALID_ACCEPTED_TOTAL')]").isNotEmpty());

    mockMvc
        .perform(
            post("/api/booking-requests")
                .header("Idempotency-Key", "money-limit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validSubmitJson(guesthouseId, roomTypeId, "10000000000.00")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.code == 'INVALID_ACCEPTED_TOTAL')]").isNotEmpty());

    mockMvc
        .perform(
            post("/api/booking-requests")
                .header("Idempotency-Key", "a".repeat(129))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validSubmitJson(guesthouseId, roomTypeId, "700.00")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].code").value("IDEMPOTENCY_KEY_TOO_LONG"));

    assertEquals(0, count("booking_request"));
  }

  @Test
  void returnsUpdatedQuoteWhenAcceptedPriceIsStaleAndPersistsNothing() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    setPrice("bukovina-panzio", "accommodation", "140.00");

    mockMvc
        .perform(
            post("/api/booking-requests")
                .header("Idempotency-Key", "stale-price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validSubmitJson(guesthouseId, roomTypeId, "700.00")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("BOOKING_PRICE_CHANGED"))
        .andExpect(jsonPath("$.currentQuote.priceBreakdown.accommodationTotal").value(560.00))
        .andExpect(jsonPath("$.currentQuote.priceBreakdown.totalPayable").value(740.00));
    assertEquals(0, count("booking_request"));
  }

  @Test
  void anonymouslyPersistsCompleteReceivedRequestAndLiteralNoteAtomically() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    addNotificationRecipient(guesthouseId, "ADMIN@EXAMPLE.COM");

    mockMvc
        .perform(
            post("/api/booking-requests")
                .header("Idempotency-Key", "successful-booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validSubmitJson(guesthouseId, roomTypeId, "700.00")))
        .andExpect(status().isCreated())
        .andExpect(
            jsonPath("$.reference").value(org.hamcrest.Matchers.matchesPattern("NB-[A-F0-9]{16}")))
        .andExpect(jsonPath("$.status").value("RECEIVED"))
        .andExpect(jsonPath("$.currency").value("RON"))
        .andExpect(jsonPath("$.nights").value(2))
        .andExpect(jsonPath("$.totalGuests").value(2))
        .andExpect(jsonPath("$.totalPayable").value(700.00))
        .andExpect(jsonPath("$.managementTokenHash").doesNotExist())
        .andExpect(jsonPath("$.note").doesNotExist())
        .andExpect(jsonPath("$.requestOnly").value(true));

    assertEquals(1, count("booking_request"));
    assertEquals(1, count("booking_room_selection"));
    assertEquals(1, count("booking_status_history"));
    assertEquals(2, count("notification_outbox"));
    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification_outbox WHERE notification_type = 'BOOKING_RECEIVED_GUEST' AND recipient = 'guest@example.com' AND encrypted_management_token IS NOT NULL",
            Integer.class));
    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification_outbox WHERE notification_type = 'BOOKING_RECEIVED_ADMIN' AND recipient = 'admin@example.com'",
            Integer.class));
    assertEquals(
        "RECEIVED",
        jdbcTemplate.queryForObject("SELECT status FROM booking_request", String.class));
    assertEquals(
        "<script>alert(1)</script>",
        jdbcTemplate.queryForObject("SELECT note FROM booking_request", String.class));
    assertEquals(
        "700.00",
        jdbcTemplate.queryForObject(
            "SELECT total_payable::TEXT FROM booking_request", String.class));
  }

  @Test
  void protectsAgainstDuplicateSubmissionAndIdempotencyKeyReuse() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    String body = validSubmitJson(guesthouseId, roomTypeId, "700.00");

    MvcResult first = submit(body, "same-logical-request");
    setPrice("bukovina-panzio", "accommodation", "140.00");
    MvcResult second = submit(body, "same-logical-request");
    assertEquals(201, first.getResponse().getStatus());
    assertEquals(201, second.getResponse().getStatus());
    assertEquals(
        first.getResponse().getContentAsString(), second.getResponse().getContentAsString());
    assertEquals(1, count("booking_request"));
    assertEquals(1, count("notification_outbox"));

    String changedBody = body.replace("  Teszt   Vendeg  ", "Masik Vendeg");
    mockMvc
        .perform(
            post("/api/booking-requests")
                .header("Idempotency-Key", "same-logical-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(changedBody))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
    assertEquals(1, count("booking_request"));
    assertEquals(1, count("notification_outbox"));
  }

  @Test
  void snapshotsGuesthouseRecipientsForNewBookingsAndAllowsSharedAddresses() throws Exception {
    UUID bukovinaId = guesthouseId("bukovina-panzio");
    UUID nisztorId = guesthouseId("nisztor-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    addNotificationRecipient(bukovinaId, "shared@example.com");
    addNotificationRecipient(nisztorId, "shared@example.com");

    submit(validSubmitJson(bukovinaId, roomTypeId, "700.00"), "recipient-snapshot-one");
    jdbcTemplate.update(
        "UPDATE guesthouse_notification_recipient SET active = FALSE WHERE guesthouse_id = ?",
        bukovinaId);
    addNotificationRecipient(bukovinaId, "new-admin@example.com");
    submit(validSubmitJson(bukovinaId, roomTypeId, "700.00"), "recipient-snapshot-two");

    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification_outbox WHERE notification_type = 'BOOKING_RECEIVED_ADMIN' AND recipient = 'shared@example.com'",
            Integer.class));
    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification_outbox WHERE notification_type = 'BOOKING_RECEIVED_ADMIN' AND recipient = 'new-admin@example.com'",
            Integer.class));
    assertEquals(
        2,
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM guesthouse_notification_recipient WHERE email = 'shared@example.com'",
            Integer.class));
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void serializesConcurrentSubmissionsWithTheSameIdempotencyKey() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");
    String email = "parallel@example.com";
    String body =
        submitJson(
            guesthouseId,
            roomTypeId,
            "Parallel Guest",
            email,
            "+40 700 000 001",
            "en",
            null,
            "700.00");
    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
      List<Future<MvcResult>> results =
          executor.invokeAll(
              List.of(
                  submission(body, "parallel-idempotency-key"),
                  submission(body, "parallel-idempotency-key")));

      MvcResult first = results.getFirst().get();
      MvcResult second = results.get(1).get();
      assertEquals(201, first.getResponse().getStatus());
      assertEquals(201, second.getResponse().getStatus());
      assertEquals(
          first.getResponse().getContentAsString(), second.getResponse().getContentAsString());
      assertEquals(1, count("booking_request"));
    } finally {
      executor.shutdownNow();
      jdbcTemplate.update("DELETE FROM booking_request WHERE contact_email = ?", email);
    }
  }

  @Test
  void requiresAnIdempotencyKeyAndRollsBackInvalidCreation() throws Exception {
    UUID guesthouseId = guesthouseId("bukovina-panzio");
    UUID roomTypeId = roomTypeId("bukovina-panzio", "double");

    mockMvc
        .perform(
            post("/api/booking-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validSubmitJson(guesthouseId, roomTypeId, "700.00")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].code").value("IDEMPOTENCY_KEY_REQUIRED"));
    assertEquals(0, count("booking_request"));
    assertEquals(0, count("booking_room_selection"));
    assertEquals(0, count("booking_status_history"));
  }

  private MvcResult submit(String body, String idempotencyKey) throws Exception {
    return mockMvc
        .perform(
            post("/api/booking-requests")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andReturn();
  }

  private Callable<MvcResult> submission(String body, String idempotencyKey) {
    return () -> submit(body, idempotencyKey);
  }

  private void expectError(String body, String code) throws Exception {
    mockMvc
        .perform(post("/api/booking-quotes").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.code == '" + code + "')]").isNotEmpty());
  }

  private String validSubmitJson(UUID guesthouseId, UUID roomTypeId, String acceptedTotal) {
    return submitJson(
        guesthouseId,
        roomTypeId,
        "  Teszt   Vendeg  ",
        "GUEST@EXAMPLE.COM",
        "+40 700 000 000",
        "hu",
        "<script>alert(1)</script>",
        acceptedTotal);
  }

  private String submitJson(
      UUID guesthouseId,
      UUID roomTypeId,
      String name,
      String email,
      String phone,
      String language,
      String note,
      String acceptedTotal) {
    return """
        {
          "guesthouseId": "%s",
          "checkInDate": "%s",
          "checkOutDate": "%s",
          "adults": 2,
          "childrenAge3to10": 0,
          "childrenAge0to3": 0,
          "roomSelections": [{"roomTypeId": "%s", "quantity": 1}],
          "services": {"breakfastParticipants": 2, "dinnerParticipants": 0},
          "contactName": "%s",
          "contactEmail": "%s",
          "contactPhone": "%s",
          "preferredLanguage": "%s",
          "note": "%s",
          "acceptedTotal": %s
        }
        """
        .formatted(
            guesthouseId,
            futureCheckIn(),
            futureCheckOut(),
            roomTypeId,
            name,
            email,
            phone,
            language,
            note,
            acceptedTotal);
  }

  private String quoteJson(
      UUID guesthouseId,
      UUID roomTypeId,
      int adults,
      int childrenAge3to10,
      int childrenAge0to3,
      int roomQuantity,
      int breakfastParticipants,
      int dinnerParticipants) {
    return """
        {
          "guesthouseId": "%s",
          "checkInDate": "%s",
          "checkOutDate": "%s",
          "adults": %d,
          "childrenAge3to10": %d,
          "childrenAge0to3": %d,
          "roomSelections": [{"roomTypeId": "%s", "quantity": %d}],
          "services": {"breakfastParticipants": %d, "dinnerParticipants": %d}
        }
        """
        .formatted(
            guesthouseId,
            futureCheckIn(),
            futureCheckOut(),
            adults,
            childrenAge3to10,
            childrenAge0to3,
            roomTypeId,
            roomQuantity,
            breakfastParticipants,
            dinnerParticipants);
  }

  private LocalDate futureCheckIn() {
    return LocalDate.now().plusDays(10);
  }

  private LocalDate futureCheckOut() {
    return futureCheckIn().plusDays(2);
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

  private void setPrice(String guesthouseSlug, String code, String amount) {
    int changed =
        jdbcTemplate.update(
            """
            UPDATE price_item item
            SET amount = CAST(? AS NUMERIC)
            FROM guesthouse_pricing pricing, guesthouse
            WHERE item.pricing_id = pricing.id
              AND pricing.guesthouse_id = guesthouse.id
              AND guesthouse.slug = ?
              AND item.code = ?
            """,
            amount,
            guesthouseSlug,
            code);
    assertNotEquals(0, changed);
  }

  private void setPriceActive(String guesthouseSlug, String code, boolean active) {
    jdbcTemplate.update(
        """
        UPDATE price_item item
        SET active = ?
        FROM guesthouse_pricing pricing, guesthouse
        WHERE item.pricing_id = pricing.id
          AND pricing.guesthouse_id = guesthouse.id
          AND guesthouse.slug = ?
          AND item.code = ?
        """,
        active,
        guesthouseSlug,
        code);
  }

  private void addNotificationRecipient(UUID guesthouseId, String email) {
    jdbcTemplate.update(
        "INSERT INTO guesthouse_notification_recipient (id, guesthouse_id, email) VALUES (?, ?, LOWER(?))",
        UUID.randomUUID(),
        guesthouseId,
        email);
  }

  private int count(String table) {
    return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
  }
}
