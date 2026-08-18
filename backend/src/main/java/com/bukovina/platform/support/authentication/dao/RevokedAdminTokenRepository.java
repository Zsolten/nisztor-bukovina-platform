package com.bukovina.platform.support.authentication.dao;

import com.bukovina.platform.support.authentication.model.RevokedAdminToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedAdminTokenRepository extends JpaRepository<RevokedAdminToken, UUID> {}
