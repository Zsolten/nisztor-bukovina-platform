package com.bukovina.platform.accommodation.booking.dao;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class BookingIdempotencyGuard {

  private final JdbcClient jdbcClient;

  public BookingIdempotencyGuard(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public void lock(String idempotencyKeyHash) {
    jdbcClient
        .sql("SELECT pg_advisory_xact_lock(hashtextextended(:keyHash, 0))")
        .param("keyHash", idempotencyKeyHash)
        .query((resultSet, rowNumber) -> 0)
        .single();
  }
}
