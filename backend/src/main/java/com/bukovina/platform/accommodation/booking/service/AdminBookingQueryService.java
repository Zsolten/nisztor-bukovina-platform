package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.dao.AdminBookingQueryDao;
import com.bukovina.platform.accommodation.booking.dto.AdminBookingDetailResponse;
import com.bukovina.platform.accommodation.booking.dto.AdminBookingPageResponse;
import com.bukovina.platform.accommodation.booking.dto.AdminBookingSummaryResponse;
import com.bukovina.platform.accommodation.booking.model.BookingStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminBookingQueryService {

  public static final int MAX_PAGE_SIZE = 100;

  private final AdminBookingQueryDao queryDao;

  public AdminBookingQueryService(AdminBookingQueryDao queryDao) {
    this.queryDao = queryDao;
  }

  public AdminBookingPageResponse list(
      UUID guesthouseId,
      BookingStatus status,
      LocalDate createdFrom,
      LocalDate createdTo,
      int page,
      int size,
      String sortBy,
      String sortDirection) {
    validateFilters(createdFrom, createdTo, page, size, sortBy, sortDirection);
    long totalElements = queryDao.count(guesthouseId, status, createdFrom, createdTo);
    var content =
        queryDao
            .findPage(
                guesthouseId, status, createdFrom, createdTo, page, size, sortBy, sortDirection)
            .stream()
            .map(AdminBookingSummaryResponse::from)
            .toList();
    int totalPages = totalElements == 0 ? 0 : (int) ((totalElements + size - 1) / size);
    return new AdminBookingPageResponse(content, page, size, totalElements, totalPages);
  }

  public AdminBookingDetailResponse detail(UUID bookingId) {
    return queryDao
        .findDetail(bookingId)
        .map(AdminBookingDetailResponse::from)
        .orElseThrow(() -> new AdminBookingNotFoundException(bookingId));
  }

  private void validateFilters(
      LocalDate createdFrom,
      LocalDate createdTo,
      int page,
      int size,
      String sortBy,
      String sortDirection) {
    if (page < 0) {
      throw new AdminBookingQueryValidationException("page must be zero or greater");
    }
    if (size < 1 || size > MAX_PAGE_SIZE) {
      throw new AdminBookingQueryValidationException("size must be between 1 and " + MAX_PAGE_SIZE);
    }
    if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
      throw new AdminBookingQueryValidationException(
          "createdFrom must be before or equal to createdTo");
    }
    if (!AdminBookingQueryDao.supportsSort(sortBy, sortDirection)) {
      throw new AdminBookingQueryValidationException("unsupported booking sort");
    }
  }
}
