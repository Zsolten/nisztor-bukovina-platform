package com.bukovina.platform.accommodation.booking.dao;

import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import com.bukovina.platform.accommodation.booking.service.AdminBookingDetailView;
import com.bukovina.platform.accommodation.booking.service.AdminBookingRoomView;
import com.bukovina.platform.accommodation.booking.service.AdminBookingStatusHistoryView;
import com.bukovina.platform.accommodation.booking.service.AdminBookingSummaryView;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AdminBookingQueryDao {

  private static final String FILTERED_BOOKINGS =
      """
      FROM booking_request booking
      JOIN guesthouse ON guesthouse.id = booking.guesthouse_id
      JOIN guesthouse_translation guesthouse_name
        ON guesthouse_name.guesthouse_id = guesthouse.id
       AND guesthouse_name.language_code = 'hu'
      WHERE 1 = 1
      """;

  private final JdbcClient jdbcClient;

  public AdminBookingQueryDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public List<AdminBookingSummaryView> findPage(
      UUID guesthouseId,
      BookingStatus status,
      LocalDate createdFrom,
      LocalDate createdTo,
      int page,
      int size) {
    Filter filter = filter(guesthouseId, status, createdFrom, createdTo);
    String sql =
        """
        SELECT booking.id, booking.public_reference, booking.guesthouse_id,
               guesthouse_name.name AS guesthouse_name, booking.status,
               booking.check_in_date, booking.check_out_date,
               booking.adults, booking.children_age_3_to_10, booking.children_age_0_to_3,
               booking.contact_name, booking.total_payable, booking.currency, booking.created_at
        """
            + FILTERED_BOOKINGS
            + filter.where()
            + " ORDER BY booking.created_at DESC, booking.id DESC LIMIT :limit OFFSET :offset";
    Map<String, Object> parameters = new HashMap<>(filter.parameters());
    parameters.put("limit", size);
    parameters.put("offset", (long) page * size);

    return bind(jdbcClient.sql(sql), parameters)
        .query(
            (resultSet, rowNumber) ->
                new AdminBookingSummaryView(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getString("public_reference"),
                    resultSet.getObject("guesthouse_id", UUID.class),
                    resultSet.getString("guesthouse_name"),
                    BookingStatus.valueOf(resultSet.getString("status")),
                    resultSet.getObject("check_in_date", LocalDate.class),
                    resultSet.getObject("check_out_date", LocalDate.class),
                    resultSet.getInt("adults"),
                    resultSet.getInt("children_age_3_to_10"),
                    resultSet.getInt("children_age_0_to_3"),
                    resultSet.getString("contact_name"),
                    resultSet.getBigDecimal("total_payable"),
                    resultSet.getString("currency"),
                    resultSet.getTimestamp("created_at").toInstant()))
        .list();
  }

  public long count(
      UUID guesthouseId, BookingStatus status, LocalDate createdFrom, LocalDate createdTo) {
    Filter filter = filter(guesthouseId, status, createdFrom, createdTo);
    String sql = "SELECT COUNT(*) " + FILTERED_BOOKINGS + filter.where();
    return bind(jdbcClient.sql(sql), filter.parameters()).query(Long.class).single();
  }

  public Optional<AdminBookingDetailView> findDetail(UUID bookingId) {
    Optional<AdminBookingDetailView> detail =
        jdbcClient
            .sql(
                """
                SELECT booking.id, booking.public_reference, booking.guesthouse_id,
                       guesthouse_name.name AS guesthouse_name,
                       booking.check_in_date, booking.check_out_date,
                       booking.adults, booking.children_age_3_to_10,
                       booking.children_age_0_to_3, booking.breakfast_participants,
                       booking.dinner_participants, booking.contact_name,
                       booking.contact_email, booking.contact_phone,
                       booking.preferred_language, booking.note, booking.internal_note,
                       booking.status, booking.accommodation_total,
                       booking.single_room_surcharge, booking.breakfast_total,
                       booking.dinner_total, booking.total_payable, booking.currency,
                       booking.created_at, booking.updated_at
                FROM booking_request booking
                JOIN guesthouse ON guesthouse.id = booking.guesthouse_id
                JOIN guesthouse_translation guesthouse_name
                  ON guesthouse_name.guesthouse_id = guesthouse.id
                 AND guesthouse_name.language_code = 'hu'
                WHERE booking.id = :bookingId
                """)
            .param("bookingId", bookingId)
            .query(
                (resultSet, rowNumber) ->
                    new AdminBookingDetailView(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("public_reference"),
                        resultSet.getObject("guesthouse_id", UUID.class),
                        resultSet.getString("guesthouse_name"),
                        resultSet.getObject("check_in_date", LocalDate.class),
                        resultSet.getObject("check_out_date", LocalDate.class),
                        resultSet.getInt("adults"),
                        resultSet.getInt("children_age_3_to_10"),
                        resultSet.getInt("children_age_0_to_3"),
                        resultSet.getInt("breakfast_participants"),
                        resultSet.getInt("dinner_participants"),
                        resultSet.getString("contact_name"),
                        resultSet.getString("contact_email"),
                        resultSet.getString("contact_phone"),
                        resultSet.getString("preferred_language"),
                        resultSet.getString("note"),
                        resultSet.getString("internal_note"),
                        BookingStatus.valueOf(resultSet.getString("status")),
                        resultSet.getBigDecimal("accommodation_total"),
                        resultSet.getBigDecimal("single_room_surcharge"),
                        resultSet.getBigDecimal("breakfast_total"),
                        resultSet.getBigDecimal("dinner_total"),
                        resultSet.getBigDecimal("total_payable"),
                        resultSet.getString("currency"),
                        resultSet.getTimestamp("created_at").toInstant(),
                        resultSet.getTimestamp("updated_at").toInstant(),
                        List.of(),
                        List.of()))
            .optional();

    return detail.map(
        booking ->
            new AdminBookingDetailView(
                booking.id(),
                booking.publicReference(),
                booking.guesthouseId(),
                booking.guesthouseName(),
                booking.checkInDate(),
                booking.checkOutDate(),
                booking.adults(),
                booking.childrenAge3to10(),
                booking.childrenAge0to3(),
                booking.breakfastParticipants(),
                booking.dinnerParticipants(),
                booking.contactName(),
                booking.contactEmail(),
                booking.contactPhone(),
                booking.preferredLanguage(),
                booking.guestNote(),
                booking.internalNote(),
                booking.status(),
                booking.accommodationTotal(),
                booking.singleRoomSurcharge(),
                booking.breakfastTotal(),
                booking.dinnerTotal(),
                booking.totalPayable(),
                booking.currency(),
                booking.createdAt(),
                booking.updatedAt(),
                findRooms(booking.id()),
                findStatusHistory(booking.id())));
  }

  private List<AdminBookingRoomView> findRooms(UUID bookingId) {
    return jdbcClient
        .sql(
            """
            SELECT selection.room_type_id, room_name.name AS room_type_name, selection.quantity
            FROM booking_room_selection selection
            JOIN room_type_translation room_name
              ON room_name.room_type_id = selection.room_type_id
             AND room_name.language_code = 'hu'
            WHERE selection.booking_request_id = :bookingId
            ORDER BY room_name.name, selection.room_type_id
            """)
        .param("bookingId", bookingId)
        .query(
            (resultSet, rowNumber) ->
                new AdminBookingRoomView(
                    resultSet.getObject("room_type_id", UUID.class),
                    resultSet.getString("room_type_name"),
                    resultSet.getInt("quantity")))
        .list();
  }

  private List<AdminBookingStatusHistoryView> findStatusHistory(UUID bookingId) {
    return jdbcClient
        .sql(
            """
            SELECT status, changed_at, changed_by
            FROM booking_status_history
            WHERE booking_request_id = :bookingId
            ORDER BY changed_at ASC, id ASC
            """)
        .param("bookingId", bookingId)
        .query(
            (resultSet, rowNumber) ->
                new AdminBookingStatusHistoryView(
                    BookingStatus.valueOf(resultSet.getString("status")),
                    resultSet.getTimestamp("changed_at").toInstant(),
                    resultSet.getString("changed_by")))
        .list();
  }

  private Filter filter(
      UUID guesthouseId, BookingStatus status, LocalDate createdFrom, LocalDate createdTo) {
    List<String> clauses = new ArrayList<>();
    Map<String, Object> parameters = new HashMap<>();
    if (guesthouseId != null) {
      clauses.add("booking.guesthouse_id = :guesthouseId");
      parameters.put("guesthouseId", guesthouseId);
    }
    if (status != null) {
      clauses.add("booking.status = :status");
      parameters.put("status", status.name());
    }
    if (createdFrom != null) {
      clauses.add("booking.created_at >= :createdFrom");
      parameters.put(
          "createdFrom",
          Timestamp.from(createdFrom.atStartOfDay(java.time.ZoneOffset.UTC).toInstant()));
    }
    if (createdTo != null) {
      clauses.add("booking.created_at < :createdBefore");
      parameters.put(
          "createdBefore",
          Timestamp.from(createdTo.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant()));
    }
    String where = clauses.isEmpty() ? "" : " AND " + String.join(" AND ", clauses);
    return new Filter(where, parameters);
  }

  private JdbcClient.StatementSpec bind(
      JdbcClient.StatementSpec statement, Map<String, Object> parameters) {
    JdbcClient.StatementSpec bound = statement;
    for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
      bound = bound.param(parameter.getKey(), parameter.getValue());
    }
    return bound;
  }

  private record Filter(String where, Map<String, Object> parameters) {}
}
