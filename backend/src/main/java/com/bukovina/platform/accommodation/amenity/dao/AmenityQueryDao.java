package com.bukovina.platform.accommodation.amenity.dao;

import com.bukovina.platform.accommodation.amenity.service.AmenityView;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AmenityQueryDao {

  private final JdbcClient jdbcClient;

  public AmenityQueryDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public List<AmenityView> findPublished(UUID guesthouseId, String language) {
    return jdbcClient
        .sql(
            """
            SELECT amenity.code,
                   COALESCE(requested.name, fallback.name) AS name,
                   COALESCE(requested.description, fallback.description) AS description,
                   COALESCE(requested.detailed_description, fallback.detailed_description) AS detailed_description,
                   amenity.category,
                   amenity.pricing_type,
                   assignment.display_order
            FROM guesthouse_amenity assignment
            JOIN amenity ON amenity.id = assignment.amenity_id
            LEFT JOIN amenity_translation requested
              ON requested.amenity_id = amenity.id
             AND requested.language_code = :language
            JOIN amenity_translation fallback
              ON fallback.amenity_id = amenity.id
             AND fallback.language_code = 'hu'
            WHERE assignment.guesthouse_id = :guesthouseId
              AND assignment.active = TRUE
            ORDER BY assignment.display_order
            """)
        .param("guesthouseId", guesthouseId)
        .param("language", language)
        .query(
            (resultSet, rowNumber) ->
                new AmenityView(
                    resultSet.getString("code"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("detailed_description"),
                    resultSet.getString("category"),
                    resultSet.getString("pricing_type"),
                    resultSet.getInt("display_order")))
        .list();
  }
}
