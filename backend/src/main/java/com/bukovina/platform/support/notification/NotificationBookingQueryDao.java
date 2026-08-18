package com.bukovina.platform.support.notification;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationBookingQueryDao {

  private final JdbcClient jdbcClient;

  public NotificationBookingQueryDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public Optional<NotificationBookingView> findById(UUID bookingId, String language) {
    return jdbcClient
        .sql(
            """
            SELECT booking.id, booking.public_reference,
                   COALESCE(requested.name, fallback.name, guesthouse.slug) AS guesthouse_name,
                   booking.contact_name,
                   booking.check_in_date, booking.check_out_date,
                   booking.adults, booking.children_age_3_to_10, booking.children_age_0_to_3,
                   booking.breakfast_participants, booking.dinner_participants,
                   booking.accommodation_total, booking.single_room_surcharge,
                   booking.breakfast_total, booking.dinner_total,
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
            (resultSet, rowNumber) -> {
              LocalDate checkIn = resultSet.getObject("check_in_date", LocalDate.class);
              LocalDate checkOut = resultSet.getObject("check_out_date", LocalDate.class);
              return new NotificationBookingView(
                  resultSet.getObject("id", UUID.class),
                  resultSet.getString("public_reference"),
                  resultSet.getString("guesthouse_name"),
                  resultSet.getString("contact_name"),
                  checkIn,
                  checkOut,
                  ChronoUnit.DAYS.between(checkIn, checkOut),
                  resultSet.getInt("adults"),
                  resultSet.getInt("children_age_3_to_10"),
                  resultSet.getInt("children_age_0_to_3"),
                  resultSet.getInt("breakfast_participants"),
                  resultSet.getInt("dinner_participants"),
                  resultSet.getBigDecimal("accommodation_total"),
                  resultSet.getBigDecimal("single_room_surcharge"),
                  resultSet.getBigDecimal("breakfast_total"),
                  resultSet.getBigDecimal("dinner_total"),
                  resultSet.getBigDecimal("total_payable"),
                  resultSet.getString("currency"),
                  findRooms(bookingId, language));
            })
        .optional();
  }

  private List<NotificationRoomView> findRooms(UUID bookingId, String language) {
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
            ORDER BY room.display_order, room.id
            """)
        .param("bookingId", bookingId)
        .param("language", language)
        .query(
            (resultSet, rowNumber) ->
                new NotificationRoomView(
                    resultSet.getString("room_name"), resultSet.getInt("quantity")))
        .list();
  }
}
