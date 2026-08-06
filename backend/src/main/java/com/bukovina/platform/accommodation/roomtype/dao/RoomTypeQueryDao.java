package com.bukovina.platform.accommodation.roomtype.dao;

import com.bukovina.platform.accommodation.roomtype.service.RoomTypeView;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RoomTypeQueryDao {

  private final JdbcClient jdbcClient;

  public RoomTypeQueryDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public List<RoomTypeView> findPublished(UUID guesthouseId, String language) {
    return jdbcClient
        .sql(
            """
            SELECT room.id, room.code,
                   COALESCE(requested.name, fallback.name) AS name,
                   room.quantity, room.standard_occupancy,
                   room.rooms_with_extra_bed, room.extra_beds_per_eligible_room
            FROM room_type room
            LEFT JOIN room_type_translation requested
              ON requested.room_type_id = room.id
             AND requested.language_code = :language
            JOIN room_type_translation fallback
              ON fallback.room_type_id = room.id
             AND fallback.language_code = 'hu'
            WHERE room.guesthouse_id = :guesthouseId
              AND room.active = TRUE
            ORDER BY room.display_order
            """)
        .param("guesthouseId", guesthouseId)
        .param("language", language)
        .query(
            (resultSet, rowNumber) ->
                new RoomTypeView(
                    resultSet.getString("code"),
                    resultSet.getString("name"),
                    resultSet.getInt("quantity"),
                    resultSet.getInt("standard_occupancy"),
                    resultSet.getInt("rooms_with_extra_bed"),
                    resultSet.getInt("extra_beds_per_eligible_room"),
                    findFeatures(resultSet.getObject("id", UUID.class))))
        .list();
  }

  private List<String> findFeatures(UUID roomTypeId) {
    return jdbcClient
        .sql(
            """
            SELECT amenity.code
            FROM room_type_feature feature
            JOIN amenity ON amenity.id = feature.amenity_id
            WHERE feature.room_type_id = :roomTypeId
            ORDER BY feature.display_order
            """)
        .param("roomTypeId", roomTypeId)
        .query(String.class)
        .list();
  }
}
