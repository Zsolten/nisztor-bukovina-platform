package com.bukovina.platform.shared.configuration;

import com.bukovina.platform.support.authentication.AdminBootstrapProperties;
import com.bukovina.platform.support.authentication.AdminJwtAuthenticationConverter;
import com.bukovina.platform.support.authentication.AdminJwtProperties;
import com.bukovina.platform.support.authentication.RevokedAdminTokenValidator;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties({AdminJwtProperties.class, AdminBootstrapProperties.class})
public class SecurityConfiguration {

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, AdminJwtAuthenticationConverter authenticationConverter) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.POST, "/api/admin/auth/login")
                    .permitAll()
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .permitAll())
        .oauth2ResourceServer(
            resourceServer ->
                resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)));
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  JwtEncoder jwtEncoder(AdminJwtProperties properties) {
    return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey(properties)));
  }

  @Bean
  JwtDecoder jwtDecoder(
      AdminJwtProperties properties, RevokedAdminTokenValidator revokedTokenValidator) {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withSecretKey(secretKey(properties))
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(properties.issuer()), revokedTokenValidator));
    return decoder;
  }

  private SecretKey secretKey(AdminJwtProperties properties) {
    try {
      byte[] bytes = Base64.getDecoder().decode(properties.secret());
      if (bytes.length < 32) {
        throw new IllegalStateException("ADMIN_JWT_SECRET must decode to at least 32 bytes");
      }
      return new SecretKeySpec(bytes, "HmacSHA256");
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException("ADMIN_JWT_SECRET must be Base64-encoded", exception);
    }
  }
}
