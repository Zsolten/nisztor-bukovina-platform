package com.bukovina.platform.accommodation.booking.service;

import com.bukovina.platform.accommodation.booking.dto.BookingInput;
import com.bukovina.platform.accommodation.booking.dto.BookingServicesRequest;
import com.bukovina.platform.accommodation.booking.dto.RoomSelectionRequest;
import com.bukovina.platform.accommodation.guesthouse.service.GuesthouseBookingQuery;
import com.bukovina.platform.accommodation.roomtype.service.BookableRoomTypeView;
import com.bukovina.platform.accommodation.roomtype.service.BookingRoomTypeQuery;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BookingValidator {

  private static final int MAX_ROOM_SELECTIONS = 20;
  private final GuesthouseBookingQuery guesthouseQuery;
  private final BookingRoomTypeQuery roomTypeQuery;
  private final int largeGroupThreshold;
  private final Clock clock;

  @Autowired
  public BookingValidator(
      GuesthouseBookingQuery guesthouseQuery,
      BookingRoomTypeQuery roomTypeQuery,
      @Value("${booking.public.large-group-threshold}") int largeGroupThreshold) {
    this(guesthouseQuery, roomTypeQuery, largeGroupThreshold, Clock.systemDefaultZone());
  }

  BookingValidator(
      GuesthouseBookingQuery guesthouseQuery,
      BookingRoomTypeQuery roomTypeQuery,
      int largeGroupThreshold,
      Clock clock) {
    this.guesthouseQuery = guesthouseQuery;
    this.roomTypeQuery = roomTypeQuery;
    this.largeGroupThreshold = largeGroupThreshold;
    this.clock = clock;
  }

  public ValidatedBooking validate(BookingInput input) {
    if (input == null) {
      throw new BookingValidationException("REQUEST_REQUIRED", "request", "required");
    }
    List<BookingProblem> problems = new ArrayList<>();
    validateGuesthouse(input.guesthouseId(), problems);
    long nights = validateDates(input.checkInDate(), input.checkOutDate(), problems);
    int adults = nonNegative(input.adults(), "adults", problems);
    int childrenAge3to10 = nonNegative(input.childrenAge3to10(), "childrenAge3to10", problems);
    int childrenAge0to3 = nonNegative(input.childrenAge0to3(), "childrenAge0to3", problems);
    long totalGuestsLong = (long) adults + childrenAge3to10 + childrenAge0to3;
    validateGuestTotal(adults, totalGuestsLong, problems);
    int totalGuests = totalGuestsLong > Integer.MAX_VALUE ? 0 : (int) totalGuestsLong;

    List<ValidatedBooking.ValidatedRoomSelection> rooms =
        validateRooms(input.guesthouseId(), input.roomSelections(), problems);
    int selectedRoomCount =
        rooms.stream().mapToInt(ValidatedBooking.ValidatedRoomSelection::quantity).sum();
    int selectedCapacity =
        rooms.stream().mapToInt(room -> room.quantity() * room.standardOccupancy()).sum();
    int singleRoomCount =
        rooms.stream()
            .filter(room -> room.standardOccupancy() == 1)
            .mapToInt(ValidatedBooking.ValidatedRoomSelection::quantity)
            .sum();
    validateCapacity(
        adults, totalGuests, selectedRoomCount, selectedCapacity, singleRoomCount, problems);

    BookingServicesRequest services = input.services();
    int breakfastParticipants =
        validateParticipants(
            services == null ? null : services.breakfastParticipants(),
            "services.breakfastParticipants",
            totalGuests,
            problems);
    int dinnerParticipants =
        validateParticipants(
            services == null ? null : services.dinnerParticipants(),
            "services.dinnerParticipants",
            totalGuests,
            problems);

    if (!problems.isEmpty()) {
      throw new BookingValidationException(problems);
    }

    return new ValidatedBooking(
        input.guesthouseId(),
        input.checkInDate(),
        input.checkOutDate(),
        nights,
        adults,
        childrenAge3to10,
        childrenAge0to3,
        totalGuests,
        breakfastParticipants,
        dinnerParticipants,
        selectedRoomCount,
        selectedCapacity,
        singleRoomCount,
        rooms);
  }

  private void validateGuesthouse(UUID guesthouseId, List<BookingProblem> problems) {
    if (guesthouseId == null) {
      problems.add(problem("GUESTHOUSE_REQUIRED", "guesthouseId", "required"));
    } else if (!guesthouseQuery.existsActive(guesthouseId)) {
      problems.add(problem("GUESTHOUSE_NOT_AVAILABLE", "guesthouseId", "activeGuesthouse"));
    }
  }

  private long validateDates(
      LocalDate checkInDate, LocalDate checkOutDate, List<BookingProblem> problems) {
    if (checkInDate == null) {
      problems.add(problem("CHECK_IN_REQUIRED", "checkInDate", "required"));
    } else if (checkInDate.isBefore(LocalDate.now(clock))) {
      problems.add(problem("CHECK_IN_IN_PAST", "checkInDate", "notPast"));
    }
    if (checkOutDate == null) {
      problems.add(problem("CHECK_OUT_REQUIRED", "checkOutDate", "required"));
    }
    if (checkInDate != null && checkOutDate != null && !checkOutDate.isAfter(checkInDate)) {
      problems.add(problem("INVALID_DATE_RANGE", "checkOutDate", "afterCheckIn"));
    }
    return checkInDate != null && checkOutDate != null && checkOutDate.isAfter(checkInDate)
        ? ChronoUnit.DAYS.between(checkInDate, checkOutDate)
        : 0;
  }

  private int nonNegative(Integer value, String field, List<BookingProblem> problems) {
    if (value == null) {
      problems.add(problem("GUEST_COUNT_REQUIRED", field, "required"));
      return 0;
    }
    if (value < 0) {
      problems.add(problem("NEGATIVE_GUEST_COUNT", field, "nonNegative"));
      return 0;
    }
    return value;
  }

  private void validateGuestTotal(int adults, long totalGuests, List<BookingProblem> problems) {
    if (totalGuests == 0) {
      problems.add(problem("TOTAL_GUESTS_REQUIRED", "guestCounts", "positiveTotal"));
    } else if (adults == 0) {
      problems.add(problem("ADULT_REQUIRED", "adults", "adultRequired"));
    }
    if (totalGuests > largeGroupThreshold) {
      problems.add(problem("LARGE_GROUP_OFFLINE_ONLY", "guestCounts", "largeGroupThreshold"));
    }
    if (totalGuests > Integer.MAX_VALUE) {
      problems.add(problem("GUEST_COUNT_TOO_LARGE", "guestCounts", "integerRange"));
    }
  }

  private List<ValidatedBooking.ValidatedRoomSelection> validateRooms(
      UUID guesthouseId, List<RoomSelectionRequest> requestedRooms, List<BookingProblem> problems) {
    if (requestedRooms == null || requestedRooms.isEmpty()) {
      problems.add(problem("ROOM_SELECTION_REQUIRED", "roomSelections", "minItems"));
      return List.of();
    }
    if (requestedRooms.size() > MAX_ROOM_SELECTIONS) {
      problems.add(problem("TOO_MANY_ROOM_SELECTIONS", "roomSelections", "maxItems"));
      return List.of();
    }

    List<ValidatedBooking.ValidatedRoomSelection> rooms = new ArrayList<>();
    Set<UUID> seenRoomTypeIds = new HashSet<>();
    for (int index = 0; index < requestedRooms.size(); index++) {
      RoomSelectionRequest requested = requestedRooms.get(index);
      String field = "roomSelections[" + index + "]";
      if (requested == null || requested.roomTypeId() == null) {
        problems.add(problem("ROOM_TYPE_REQUIRED", field + ".roomTypeId", "required"));
        continue;
      }
      if (!seenRoomTypeIds.add(requested.roomTypeId())) {
        problems.add(problem("DUPLICATE_ROOM_TYPE", field + ".roomTypeId", "unique"));
        continue;
      }
      if (requested.quantity() == null) {
        problems.add(problem("ROOM_QUANTITY_REQUIRED", field + ".quantity", "required"));
        continue;
      }
      if (requested.quantity() <= 0) {
        problems.add(problem("INVALID_ROOM_QUANTITY", field + ".quantity", "positive"));
        continue;
      }

      BookableRoomTypeView roomType = roomTypeQuery.findById(requested.roomTypeId()).orElse(null);
      if (roomType == null) {
        problems.add(problem("ROOM_TYPE_NOT_FOUND", field + ".roomTypeId", "exists"));
      } else if (guesthouseId == null || !roomType.guesthouseId().equals(guesthouseId)) {
        problems.add(
            problem("ROOM_TYPE_GUESTHOUSE_MISMATCH", field + ".roomTypeId", "belongsToGuesthouse"));
      } else if (!roomType.active()) {
        problems.add(problem("ROOM_TYPE_NOT_BOOKABLE", field + ".roomTypeId", "active"));
      } else if (requested.quantity() > roomType.quantity()) {
        problems.add(problem("ROOM_QUANTITY_EXCEEDS_STOCK", field + ".quantity", "physicalStock"));
      } else {
        rooms.add(
            new ValidatedBooking.ValidatedRoomSelection(
                roomType.id(), requested.quantity(), roomType.standardOccupancy()));
      }
    }
    return rooms;
  }

  private void validateCapacity(
      int adults,
      int totalGuests,
      int selectedRoomCount,
      int selectedCapacity,
      int singleRoomCount,
      List<BookingProblem> problems) {
    if (selectedCapacity < totalGuests) {
      problems.add(problem("INSUFFICIENT_ROOM_CAPACITY", "roomSelections", "guestCapacity"));
    }
    if (selectedRoomCount > totalGuests) {
      problems.add(problem("TOO_MANY_ROOMS", "roomSelections", "guestPerRoom"));
    }
    if (singleRoomCount > adults) {
      problems.add(problem("TOO_MANY_SINGLE_ROOMS", "roomSelections", "singleRoomRequiresAdult"));
    }
  }

  private int validateParticipants(
      Integer requested, String field, int totalGuests, List<BookingProblem> problems) {
    if (requested == null) {
      return 0;
    }
    if (requested < 0) {
      problems.add(problem("NEGATIVE_SERVICE_PARTICIPANTS", field, "nonNegative"));
      return 0;
    }
    if (requested > totalGuests) {
      problems.add(problem("SERVICE_PARTICIPANTS_EXCEED_GUESTS", field, "guestCountMaximum"));
    }
    return requested;
  }

  private BookingProblem problem(String code, String field, String rule) {
    return new BookingProblem(code, field, rule);
  }
}
