package com.bukovina.platform.support.authentication;

import com.bukovina.platform.support.authentication.dao.AdminAccountRepository;
import com.bukovina.platform.support.authentication.dto.AdminLoginRequest;
import com.bukovina.platform.support.authentication.dto.AdminLoginResponse;
import com.bukovina.platform.support.authentication.model.AdminAccount;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminJwtService {

  private static final int EMAIL_MAX_LENGTH = 320;
  private static final int PASSWORD_MAX_LENGTH = 256;

  private final AdminAccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtEncoder jwtEncoder;
  private final AdminJwtProperties properties;

  public AdminJwtService(
      AdminAccountRepository accountRepository,
      PasswordEncoder passwordEncoder,
      JwtEncoder jwtEncoder,
      AdminJwtProperties properties) {
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtEncoder = jwtEncoder;
    this.properties = properties;
  }

  @Transactional(readOnly = true)
  public AdminLoginResponse login(AdminLoginRequest request) {
    String email = normalizedEmail(request == null ? null : request.email());
    String password = request == null ? null : request.password();
    if (email == null
        || password == null
        || password.isBlank()
        || password.length() > PASSWORD_MAX_LENGTH) {
      throw new AdminAuthenticationException();
    }

    AdminAccount account = accountRepository.findByEmailIgnoreCase(email).orElse(null);
    if (account == null
        || !account.isActive()
        || !passwordEncoder.matches(password, account.getPasswordHash())) {
      throw new AdminAuthenticationException();
    }

    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plus(properties.accessTokenTtl());
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(properties.issuer())
            .subject(account.getId().toString())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .id(UUID.randomUUID().toString())
            .claim("email", account.getEmail())
            .claim("role", account.getRole().name())
            .build();
    String accessToken =
        jwtEncoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    return new AdminLoginResponse(accessToken, "Bearer", expiresAt);
  }

  private String normalizedEmail(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.strip().toLowerCase(Locale.ROOT);
    return normalized.isEmpty() || normalized.length() > EMAIL_MAX_LENGTH ? null : normalized;
  }
}
