package com.bukovina.platform.support.authentication;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bukovina.platform.support.authentication.dao.AdminAccountRepository;
import com.bukovina.platform.support.authentication.model.AdminAccount;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class ProductionAdminBootstrapTests {

  @Test
  void createsNormalizedAdministratorWhenItDoesNotExist() throws Exception {
    AdminAccountRepository repository = mock(AdminAccountRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    when(repository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("safe-password")).thenReturn("encoded-password");

    new ProductionAdminBootstrap(
            new AdminBootstrapProperties(true, " Admin@Example.com ", "safe-password"),
            repository,
            passwordEncoder)
        .run(mock(org.springframework.boot.ApplicationArguments.class));

    verify(repository).findByEmailIgnoreCase("admin@example.com");
    verify(repository).save(any(AdminAccount.class));
  }

  @Test
  void failsWithoutBothCredentials() {
    ProductionAdminBootstrap bootstrap =
        new ProductionAdminBootstrap(
            new AdminBootstrapProperties(true, "admin@example.com", ""),
            mock(AdminAccountRepository.class),
            mock(PasswordEncoder.class));

    assertThatThrownBy(
            () -> bootstrap.run(mock(org.springframework.boot.ApplicationArguments.class)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("ADMIN_BOOTSTRAP_EMAIL");
  }

  @Test
  void doesNotReplaceAnExistingAdministrator() throws Exception {
    AdminAccountRepository repository = mock(AdminAccountRepository.class);
    when(repository.findByEmailIgnoreCase("admin@example.com"))
        .thenReturn(Optional.of(mock(AdminAccount.class)));
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    new ProductionAdminBootstrap(
            new AdminBootstrapProperties(true, "admin@example.com", "safe-password"),
            repository,
            passwordEncoder)
        .run(mock(org.springframework.boot.ApplicationArguments.class));

    verify(repository).findByEmailIgnoreCase("admin@example.com");
    verify(repository, org.mockito.Mockito.never()).save(any());
    verify(passwordEncoder, org.mockito.Mockito.never()).encode(any());
  }
}
