package com.bukovina.platform.support.notification;

import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "booking.notification", name = "enabled", havingValue = "true")
public class NotificationDeliveryWorker {

  private final NotificationOutboxClaimService claimService;
  private final NotificationDeliveryService deliveryService;

  public NotificationDeliveryWorker(
      NotificationOutboxClaimService claimService, NotificationDeliveryService deliveryService) {
    this.claimService = claimService;
    this.deliveryService = deliveryService;
  }

  @Scheduled(
      fixedDelayString = "${booking.notification.worker-delay:PT10S}",
      initialDelayString = "${booking.notification.worker-delay:PT10S}")
  public void deliverEligibleNotifications() {
    for (UUID jobId : claimService.claimEligible()) {
      deliveryService.deliver(jobId);
    }
  }
}
