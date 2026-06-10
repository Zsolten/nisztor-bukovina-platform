package edu.bbte.guesthouse_platform.booking.api;

import edu.bbte.guesthouse_platform.booking.application.BookingFacade;
import edu.bbte.guesthouse_platform.booking.application.BookingRequestCommand;
import edu.bbte.guesthouse_platform.booking.application.BookingRequestResult;
import edu.bbte.guesthouse_platform.shared.api.ApiPaths;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.PUBLIC + "/bookings")
public class BookingRequestController {
    private final BookingFacade bookingFacade;

    public BookingRequestController(BookingFacade bookingFacade) {
        this.bookingFacade = bookingFacade;
    }

    @PostMapping("/requests")
    public BookingRequestResult createBookingRequest(@Valid @RequestBody BookingRequest request) {
        return bookingFacade.createBookingRequest(new BookingRequestCommand(
                request.propertySlug(),
                request.roomSlug(),
                request.guestName(),
                request.email(),
                request.arrivalDate(),
                request.departureDate(),
                request.guestCount(),
                request.preferredLanguage(),
                request.message()
        ));
    }

    public record BookingRequest(
            @NotBlank String propertySlug,
            @NotBlank String roomSlug,
            @NotBlank String guestName,
            @Email @NotBlank String email,
            @Future @NotNull LocalDate arrivalDate,
            @Future @NotNull LocalDate departureDate,
            @Min(1) int guestCount,
            @NotBlank String preferredLanguage,
            String message
    ) {
    }
}
