package com.bukovina.platform.support.authentication;

import com.bukovina.platform.support.authentication.dao.AdminAccountRepository;
import com.bukovina.platform.support.authentication.model.AdminAccount;
import com.bukovina.platform.support.authentication.model.AdminRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DevelopmentAdminBootstrap implements ApplicationRunner {

  private final AdminBootstrapProperties properties;
  private final AdminAccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  public DevelopmentAdminBootstrap(
      AdminBootstrapProperties properties,
      AdminAccountRepository accountRepository,
      PasswordEncoder passwordEncoder) {
    this.properties = properties;
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    String email = normalized(properties.email());
    String password = properties.password();
    if (email == null && (password == null || password.isBlank())) {
      return;
    }
    if (email == null || password == null || password.isBlank()) {
      throw new IllegalStateException(
          "ADMIN_BOOTSTRAP_EMAIL and ADMIN_BOOTSTRAP_PASSWORD must be configured together");
    }
    if (accountRepository.findByEmailIgnoreCase(email).isEmpty()) {
      accountRepository.save(
          new AdminAccount(email, passwordEncoder.encode(password), true, AdminRole.ADMIN));
    }
  }

  private String normalized(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.strip().toLowerCase(java.util.Locale.ROOT);
    return normalized.isBlank() ? null : normalized;
  }
}
