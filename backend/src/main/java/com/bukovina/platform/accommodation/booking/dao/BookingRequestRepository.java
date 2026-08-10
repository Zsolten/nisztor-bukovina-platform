package com.bukovina.platform.accommodation.booking.dao;

import com.bukovina.platform.accommodation.booking.model.BookingRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, UUID> {

  Optional<BookingRequest> findByIdempotencyKeyHash(String idempotencyKeyHash);
}
