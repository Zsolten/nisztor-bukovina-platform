package com.bukovina.platform.support.authentication;

import com.bukovina.platform.support.authentication.dao.RevokedAdminTokenRepository;
import com.bukovina.platform.support.authentication.model.RevokedAdminToken;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminTokenRevocationService {

  private final RevokedAdminTokenRepository repository;

  public AdminTokenRevocationService(RevokedAdminTokenRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public boolean isRevoked(UUID jti) {
    return repository.existsById(jti);
  }

  @Transactional
  public void revoke(UUID jti, Instant expiresAt) {
    if (!repository.existsById(jti)) {
      repository.save(new RevokedAdminToken(jti, expiresAt));
    }
  }
}
