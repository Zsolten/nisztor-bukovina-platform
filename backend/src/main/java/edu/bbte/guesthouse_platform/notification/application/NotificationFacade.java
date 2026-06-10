package edu.bbte.guesthouse_platform.notification.application;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationFacade {
    public void queueBookingRequestReceived(UUID bookingRequestId, String guestEmail) {
        // RabbitMQ publishing will be connected here when the notification worker is implemented.
    }
}
