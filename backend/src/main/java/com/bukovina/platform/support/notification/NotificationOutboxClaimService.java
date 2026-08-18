package com.bukovina.platform.support.notification;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationOutboxClaimService {

  private static final int BATCH_SIZE = 20;
  private static final long PROCESSING_TIMEOUT_MINUTES = 10;

  private final JdbcClient jdbcClient;
  private final NotificationOutboxRepository repository;

  public NotificationOutboxClaimService(
      JdbcClient jdbcClient, NotificationOutboxRepository repository) {
    this.jdbcClient = jdbcClient;
    this.repository = repository;
  }

  @Transactional
  public List<UUID> claimEligible() {
    Instant now = Instant.now();
    List<UUID> ids =
        jdbcClient
            .sql(
                """
                SELECT id
                FROM notification_outbox
                WHERE ((status IN ('PENDING', 'RETRY') AND next_attempt_at <= :now)
                    OR (status = 'PROCESSING' AND last_attempt_at < :staleBefore))
                ORDER BY created_at, id
                FOR UPDATE SKIP LOCKED
                LIMIT :batchSize
                """)
            .param("now", Timestamp.from(now))
            .param(
                "staleBefore",
                Timestamp.from(now.minus(PROCESSING_TIMEOUT_MINUTES, ChronoUnit.MINUTES)))
            .param("batchSize", BATCH_SIZE)
            .query(UUID.class)
            .list();
    List<NotificationOutbox> jobs = repository.findAllById(ids);
    jobs.forEach(job -> job.markProcessing(now));
    repository.flush();
    return ids;
  }
}
