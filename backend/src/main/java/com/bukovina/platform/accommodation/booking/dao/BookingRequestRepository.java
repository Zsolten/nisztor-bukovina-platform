package com.bukovina.platform.accommodation.booking.dao;

import com.bukovina.platform.accommodation.booking.model.BookingRequest;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, UUID> {

  Optional<BookingRequest> findByIdempotencyKeyHash(String idempotencyKeyHash);

  Optional<BookingRequest> findByManagementTokenHash(String managementTokenHash);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT booking FROM BookingRequest booking WHERE booking.id = :bookingId")
  Optional<BookingRequest> findForUpdateById(@Param("bookingId") UUID bookingId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT booking FROM BookingRequest booking WHERE booking.managementTokenHash = :tokenHash")
  Optional<BookingRequest> findForUpdateByManagementTokenHash(
      @Param("tokenHash") String managementTokenHash);
}
