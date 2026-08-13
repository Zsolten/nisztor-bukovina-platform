package com.bukovina.platform.accommodation.booking.dao;

import com.bukovina.platform.accommodation.booking.service.BookingManagementView;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class BookingManagementQueryDao {

  private final JdbcClient jdbcClient;

  public BookingManagementQueryDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public Optional<BookingManagementView> findById(UUID bookingId, String language) {
    return jdbcClient
        .sql(
            """
            SELECT booking.public_reference,
                   COALESCE(requested.name, fallback.name, guesthouse.slug) AS guesthouse_name,
                   booking.check_in_date, booking.check_out_date,
                   booking.adults, booking.children_age_3_to_10, booking.children_age_0_to_3,
                   booking.breakfast_participants, booking.dinner_participants,
                   booking.accommodation_total, booking.breakfast_total, booking.dinner_total,
                   booking.total_payable, booking.currency
            FROM booking_request booking
            JOIN guesthouse ON guesthouse.id = booking.guesthouse_id
            LEFT JOIN guesthouse_translation requested
              ON requested.guesthouse_id = guesthouse.id
             AND requested.language_code = :language
            LEFT JOIN guesthouse_translation fallback
              ON fallback.guesthouse_id = guesthouse.id
             AND fallback.language_code = 'hu'
            WHERE booking.id = :bookingId
            """)
        .param("bookingId", bookingId)
        .param("language", language)
        .query(
            (resultSet, rowNumber) ->
                new BookingManagementView(
                    resultSet.getString("public_reference"),
                    resultSet.getString("guesthouse_name"),
                    resultSet.getObject("check_in_date", LocalDate.class),
                    resultSet.getObject("check_out_date", LocalDate.class),
                    resultSet.getInt("adults"),
                    resultSet.getInt("children_age_3_to_10"),
                    resultSet.getInt("children_age_0_to_3"),
                    resultSet.getInt("breakfast_participants"),
                    resultSet.getInt("dinner_participants"),
                    resultSet.getBigDecimal("accommodation_total"),
                    resultSet.getBigDecimal("breakfast_total"),
                    resultSet.getBigDecimal("dinner_total"),
                    resultSet.getBigDecimal("total_payable"),
                    resultSet.getString("currency"),
                    findRooms(bookingId, language),
                    findContacts(bookingId, language)))
        .optional();
  }

  private List<BookingManagementView.Room> findRooms(UUID bookingId, String language) {
    return jdbcClient
        .sql(
            """
            SELECT COALESCE(requested.name, fallback.name, room.code) AS room_name,
                   selection.quantity
            FROM booking_room_selection selection
            JOIN room_type room ON room.id = selection.room_type_id
            LEFT JOIN room_type_translation requested
              ON requested.room_type_id = room.id
             AND requested.language_code = :language
            LEFT JOIN room_type_translation fallback
              ON fallback.room_type_id = room.id
             AND fallback.language_code = 'hu'
            WHERE selection.booking_request_id = :bookingId
            ORDER BY room_name, room.id
            """)
        .param("bookingId", bookingId)
        .param("language", language)
        .query(
            (resultSet, rowNumber) ->
                new BookingManagementView.Room(
                    resultSet.getString("room_name"), resultSet.getInt("quantity")))
        .list();
  }

  private List<BookingManagementView.Contact> findContacts(UUID bookingId, String language) {
    return jdbcClient
        .sql(
            """
            SELECT contact.type, contact.value,
                   COALESCE(requested.label, fallback.label) AS label,
                   contact.preferred
            FROM booking_request booking
            JOIN guesthouse_contact contact ON contact.guesthouse_id = booking.guesthouse_id
            LEFT JOIN guesthouse_contact_translation requested
              ON requested.contact_id = contact.id
             AND requested.language_code = :language
            LEFT JOIN guesthouse_contact_translation fallback
              ON fallback.contact_id = contact.id
             AND fallback.language_code = 'hu'
            WHERE booking.id = :bookingId
              AND contact.active = TRUE
              AND contact.type IN ('PHONE', 'EMAIL')
            ORDER BY contact.display_order
            """)
        .param("bookingId", bookingId)
        .param("language", language)
        .query(
            (resultSet, rowNumber) ->
                new BookingManagementView.Contact(
                    resultSet.getString("type"),
                    resultSet.getString("value"),
                    resultSet.getString("label"),
                    resultSet.getBoolean("preferred")))
        .list();
  }
}
