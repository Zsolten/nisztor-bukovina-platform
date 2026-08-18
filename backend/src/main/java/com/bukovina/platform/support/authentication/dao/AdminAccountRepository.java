package com.bukovina.platform.support.authentication.dao;

import com.bukovina.platform.support.authentication.model.AdminAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, UUID> {

  Optional<AdminAccount> findByEmailIgnoreCase(String email);
}
