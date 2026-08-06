package com.bukovina.platform.accommodation.guesthouse.dao;

import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseAddressResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseContactResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseImageResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class GuesthouseContentQueryDao {

  private final JdbcClient jdbcClient;

  public GuesthouseContentQueryDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public List<GuesthouseImageResponse> findImages(UUID guesthouseId, String language) {
    return jdbcClient
        .sql(
            """
            SELECT image.path,
                   COALESCE(requested.alt_text, fallback.alt_text, image.alt_text) AS alt_text,
                   image.cover
            FROM guesthouse_image image
            LEFT JOIN guesthouse_image_translation requested
              ON requested.image_id = image.id
             AND requested.language_code = :language
            LEFT JOIN guesthouse_image_translation fallback
              ON fallback.image_id = image.id
             AND fallback.language_code = 'hu'
            WHERE image.guesthouse_id = :guesthouseId
            ORDER BY image.display_order
            """)
        .param("guesthouseId", guesthouseId)
        .param("language", language)
        .query(
            (resultSet, rowNumber) ->
                new GuesthouseImageResponse(
                    resultSet.getString("path"),
                    resultSet.getString("alt_text"),
                    resultSet.getBoolean("cover")))
        .list();
  }

  public List<GuesthouseContactResponse> findContacts(UUID guesthouseId, String language) {
    return jdbcClient
        .sql(
            """
            SELECT contact.type, contact.value,
                   COALESCE(requested.label, fallback.label) AS label,
                   contact.preferred
            FROM guesthouse_contact contact
            LEFT JOIN guesthouse_contact_translation requested
              ON requested.contact_id = contact.id
             AND requested.language_code = :language
            JOIN guesthouse_contact_translation fallback
              ON fallback.contact_id = contact.id
             AND fallback.language_code = 'hu'
            WHERE contact.guesthouse_id = :guesthouseId
              AND contact.active = TRUE
            ORDER BY contact.display_order
            """)
        .param("guesthouseId", guesthouseId)
        .param("language", language)
        .query(
            (resultSet, rowNumber) ->
                new GuesthouseContactResponse(
                    resultSet.getString("type"),
                    resultSet.getString("value"),
                    resultSet.getString("label"),
                    resultSet.getBoolean("preferred")))
        .list();
  }

  public GuesthouseAddressResponse findAddress(UUID guesthouseId, String language) {
    return jdbcClient
        .sql(
            """
            SELECT COALESCE(requested.formatted_address, fallback.formatted_address) AS formatted,
                   address.latitude, address.longitude
            FROM guesthouse_address address
            LEFT JOIN guesthouse_address_translation requested
              ON requested.address_id = address.id
             AND requested.language_code = :language
            JOIN guesthouse_address_translation fallback
              ON fallback.address_id = address.id
             AND fallback.language_code = 'hu'
            WHERE address.guesthouse_id = :guesthouseId
              AND address.active = TRUE
            """)
        .param("guesthouseId", guesthouseId)
        .param("language", language)
        .query(
            (resultSet, rowNumber) ->
                new GuesthouseAddressResponse(
                    resultSet.getString("formatted"),
                    resultSet.getBigDecimal("latitude"),
                    resultSet.getBigDecimal("longitude")))
        .optional()
        .orElseThrow(() -> new IllegalStateException("Guesthouse has no active address"));
  }
}
