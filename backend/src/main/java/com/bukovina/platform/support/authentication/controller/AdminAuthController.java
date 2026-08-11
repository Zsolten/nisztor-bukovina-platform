package com.bukovina.platform.support.authentication.controller;

import com.bukovina.platform.support.authentication.AdminJwtService;
import com.bukovina.platform.support.authentication.AdminLoginRateLimiter;
import com.bukovina.platform.support.authentication.AdminTokenRevocationService;
import com.bukovina.platform.support.authentication.dto.AdminLoginRequest;
import com.bukovina.platform.support.authentication.dto.AdminLoginResponse;
import jakarta.servlet.http.HttpServletRequest;
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
  private final AdminLoginRateLimiter loginRateLimiter;
  private final AdminTokenRevocationService tokenRevocationService;

  public AdminAuthController(
      AdminJwtService jwtService,
      AdminLoginRateLimiter loginRateLimiter,
      AdminTokenRevocationService tokenRevocationService) {
    this.jwtService = jwtService;
    this.loginRateLimiter = loginRateLimiter;
    this.tokenRevocationService = tokenRevocationService;
  }

  @PostMapping("/login")
  public AdminLoginResponse login(
      @RequestBody(required = false) AdminLoginRequest request, HttpServletRequest httpRequest) {
    String clientIp = httpRequest.getRemoteAddr();
    loginRateLimiter.consume(clientIp, request);
    AdminLoginResponse response = jwtService.login(request);
    loginRateLimiter.reset(clientIp, request);
    return response;
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
    tokenRevocationService.revoke(UUID.fromString(jwt.getId()), jwt.getExpiresAt());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
