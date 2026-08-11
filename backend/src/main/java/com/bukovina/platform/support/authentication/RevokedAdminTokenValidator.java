package com.bukovina.platform.support.authentication;

import java.util.UUID;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class RevokedAdminTokenValidator implements OAuth2TokenValidator<Jwt> {

  private static final OAuth2Error REVOKED_TOKEN =
      new OAuth2Error("invalid_token", "The token has been revoked", null);

  private final AdminTokenRevocationService tokenRevocationService;

  public RevokedAdminTokenValidator(AdminTokenRevocationService tokenRevocationService) {
    this.tokenRevocationService = tokenRevocationService;
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt token) {
    if (token.getId() == null) {
      return OAuth2TokenValidatorResult.failure(REVOKED_TOKEN);
    }
    try {
      UUID jti = UUID.fromString(token.getId());
      return tokenRevocationService.isRevoked(jti)
          ? OAuth2TokenValidatorResult.failure(REVOKED_TOKEN)
          : OAuth2TokenValidatorResult.success();
    } catch (IllegalArgumentException exception) {
      return OAuth2TokenValidatorResult.failure(REVOKED_TOKEN);
    }
  }
}
