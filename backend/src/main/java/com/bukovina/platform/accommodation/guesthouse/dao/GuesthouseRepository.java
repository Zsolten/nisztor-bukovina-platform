package com.bukovina.platform.accommodation.guesthouse.dao;

import com.bukovina.platform.accommodation.guesthouse.model.Guesthouse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuesthouseRepository extends JpaRepository<Guesthouse, UUID> {

  List<Guesthouse> findAllByActiveTrueOrderByDisplayOrderAsc();

  Optional<Guesthouse> findBySlugAndActiveTrue(String slug);
}
