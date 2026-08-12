package com.bukovina.platform.support.notification;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class GuesthouseNotificationConfigurationDao {

  private final JdbcClient jdbcClient;

  public GuesthouseNotificationConfigurationDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public List<String> findActiveRecipients(UUID guesthouseId) {
    return jdbcClient
        .sql(
            """
            SELECT DISTINCT LOWER(TRIM(email)) AS email
            FROM guesthouse_notification_recipient
            WHERE guesthouse_id = :guesthouseId
              AND active = TRUE
            ORDER BY email
            """)
        .param("guesthouseId", guesthouseId)
        .query(String.class)
        .list();
  }

  public String findPublicReplyTo(UUID guesthouseId) {
    return jdbcClient
        .sql(
            """
            SELECT LOWER(TRIM(value))
            FROM guesthouse_contact
            WHERE guesthouse_id = :guesthouseId
              AND type = 'EMAIL'
              AND active = TRUE
            ORDER BY preferred DESC, display_order ASC
            LIMIT 1
            """)
        .param("guesthouseId", guesthouseId)
        .query(String.class)
        .optional()
        .map(email -> email.toLowerCase(Locale.ROOT))
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Guesthouse notification Reply-To address is not configured"));
  }
}
