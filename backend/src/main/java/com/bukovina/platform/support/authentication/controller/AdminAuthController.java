package com.bukovina.platform.support.authentication.controller;

import com.bukovina.platform.support.authentication.AdminJwtService;
import com.bukovina.platform.support.authentication.AdminTokenRevocationService;
import com.bukovina.platform.support.authentication.dto.AdminLoginRequest;
import com.bukovina.platform.support.authentication.dto.AdminLoginResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

  private final AdminJwtService jwtService;
  private final AdminTokenRevocationService tokenRevocationService;

  public AdminAuthController(
      AdminJwtService jwtService, AdminTokenRevocationService tokenRevocationService) {
    this.jwtService = jwtService;
    this.tokenRevocationService = tokenRevocationService;
  }

  @PostMapping("/login")
  public AdminLoginResponse login(@RequestBody(required = false) AdminLoginRequest request) {
    return jwtService.login(request);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
    tokenRevocationService.revoke(UUID.fromString(jwt.getId()), jwt.getExpiresAt());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
