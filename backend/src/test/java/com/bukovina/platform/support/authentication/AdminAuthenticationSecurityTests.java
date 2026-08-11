package com.bukovina.platform.support.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bukovina.platform.support.authentication.dao.AdminAccountRepository;
import com.bukovina.platform.support.authentication.model.AdminAccount;
import com.bukovina.platform.support.authentication.model.AdminRole;
import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@AutoConfigureMockMvc
@Import({
  PostgreSqlTestContainerConfiguration.class,
  AdminAuthenticationSecurityTests.TestAdminEndpoint.class
})
class AdminAuthenticationSecurityTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private AdminAccountRepository accountRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JwtEncoder jwtEncoder;

  @Test
  void rejectsAnonymousAccessToTheAdminApiNamespace() throws Exception {
    mockMvc.perform(get("/api/admin/bookings")).andExpect(status().isUnauthorized());
  }

  @Test
  void returnsGenericAuthenticationFeedbackForInvalidCredentials() throws Exception {
    mockMvc
        .perform(
            post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"missing@example.com\",\"password\":\"wrong-password\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_ADMIN_CREDENTIALS"));
  }

  @Test
  void limitsRepeatedLoginAttemptsFromTheSameClientForTheSameEmail() throws Exception {
    String request = "{\"email\":\"limited@example.com\",\"password\":\"wrong-password\"}";
    for (int attempt = 0; attempt < 5; attempt++) {
      mockMvc
          .perform(
              post("/api/admin/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(request))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value("INVALID_ADMIN_CREDENTIALS"));
    }

    mockMvc
        .perform(
            post("/api/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"))
        .andExpect(jsonPath("$.code").value("ADMIN_LOGIN_RATE_LIMITED"))
        .andExpect(jsonPath("$.retryAfter").doesNotExist());
  }

  @Test
  void authenticatesActiveAdministratorsAndRejectsNonAdminTokens() throws Exception {
    accountRepository.save(
        new AdminAccount(
            "admin@example.com",
            passwordEncoder.encode("correct-password"),
            true,
            AdminRole.ADMIN));
    String token =
        mockMvc
            .perform(
                post("/api/admin/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"admin@example.com\",\"password\":\"correct-password\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andReturn()
            .getResponse()
            .getContentAsString()
            .replaceAll(".*\\\"accessToken\\\":\\\"([^\\\"]+)\\\".*", "$1");

    mockMvc
        .perform(get("/api/admin/test/secured").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            get("/api/admin/test/secured")
                .header(
                    "Authorization",
                    "Bearer " + tokenForRole("USER", Instant.now().plus(10, ChronoUnit.MINUTES))))
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectsExpiredTokensAndRevokesTokensOnLogout() throws Exception {
    accountRepository.save(
        new AdminAccount(
            "logout@example.com",
            passwordEncoder.encode("correct-password"),
            true,
            AdminRole.ADMIN));
    String token = login("logout@example.com", "correct-password");

    mockMvc
        .perform(post("/api/admin/auth/logout").header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
    mockMvc
        .perform(get("/api/admin/test/secured").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
    mockMvc
        .perform(
            get("/api/admin/test/secured")
                .header(
                    "Authorization",
                    "Bearer " + tokenForRole("ADMIN", Instant.now().minus(2, ChronoUnit.MINUTES))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void rejectsTokensWithoutATokenIdentifier() throws Exception {
    Instant now = Instant.now();
    String token =
        jwtEncoder
            .encode(
                JwtEncoderParameters.from(
                    JwsHeader.with(MacAlgorithm.HS256).build(),
                    JwtClaimsSet.builder()
                        .issuer("nisztor-bukovina-platform-test")
                        .subject("test-admin")
                        .issuedAt(now)
                        .expiresAt(now.plus(10, ChronoUnit.MINUTES))
                        .claim("role", "ADMIN")
                        .build()))
            .getTokenValue();

    mockMvc
        .perform(get("/api/admin/test/secured").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  private String login(String email, String password) throws Exception {
    return mockMvc
        .perform(
            post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString()
        .replaceAll(".*\\\"accessToken\\\":\\\"([^\\\"]+)\\\".*", "$1");
  }

  private String tokenForRole(String role, Instant expiresAt) {
    Instant issuedAt = expiresAt.minus(10, ChronoUnit.MINUTES);
    return jwtEncoder
        .encode(
            JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                    .issuer("nisztor-bukovina-platform-test")
                    .subject("test-admin")
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt)
                    .id(java.util.UUID.randomUUID().toString())
                    .claim("role", role)
                    .build()))
        .getTokenValue();
  }

  @RestController
  static class TestAdminEndpoint {

    @GetMapping("/api/admin/test/secured")
    String secured() {
      return "ok";
    }
  }
}
