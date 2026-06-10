package edu.bbte.guesthouse_platform.booking.application;

import edu.bbte.guesthouse_platform.booking.domain.BookingStatus;
import edu.bbte.guesthouse_platform.notification.application.NotificationFacade;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookingFacade {
    private final NotificationFacade notificationFacade;

    public BookingFacade(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    public BookingRequestResult createBookingRequest(BookingRequestCommand command) {
        UUID requestId = UUID.randomUUID();
        notificationFacade.queueBookingRequestReceived(requestId, command.email());
        return new BookingRequestResult(requestId, BookingStatus.RECEIVED);
    }
}
