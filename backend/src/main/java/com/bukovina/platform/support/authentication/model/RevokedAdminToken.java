package com.bukovina.platform.support.authentication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "revoked_admin_token")
public class RevokedAdminToken {

  @Id private UUID jti;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at", nullable = false)
  private Instant revokedAt;

  protected RevokedAdminToken() {}

  public RevokedAdminToken(UUID jti, Instant expiresAt) {
    this.jti = Objects.requireNonNull(jti);
    this.expiresAt = Objects.requireNonNull(expiresAt);
    this.revokedAt = Instant.now();
  }
}
