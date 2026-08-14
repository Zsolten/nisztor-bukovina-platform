package com.bukovina.platform.accommodation.guesthouse.dao;

import com.bukovina.platform.accommodation.guesthouse.model.GuesthouseTranslation;
import com.bukovina.platform.accommodation.guesthouse.model.GuesthouseTranslationId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuesthouseTranslationRepository
    extends JpaRepository<GuesthouseTranslation, GuesthouseTranslationId> {}
