package com.bukovina.platform.support.notification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BookingNotificationEmailFactoryTests {

  private final BookingNotificationEmailFactory factory =
      new BookingNotificationEmailFactory(properties());

  @Test
  void createsResponsiveGuestTemplateAndOmitsZeroValueRows() {
    NotificationEmailContent content =
        factory.guest(booking(), "hu", "raw-token", "nisztorpanzio@gmail.com");

    assertTrue(content.plainTextBody().contains("Foglalás kezelése"));
    assertTrue(content.htmlBody().contains("class=\"action-button\""));
    assertTrue(
        content
            .htmlBody()
            .contains("href=\"https://example.com/hu/booking-management/raw-token\""));
    assertTrue(content.htmlBody().contains("nisztorpanzio@gmail.com"));
    assertTrue(content.htmlBody().contains("@media only screen and (max-width:620px)"));
    assertTrue(content.htmlBody().contains("700 RON"));
    assertFalse(content.htmlBody().contains("Gyermekek (3–10 év)"));
    assertFalse(content.htmlBody().contains("Gyermekek (0–3 év)"));
    assertFalse(content.htmlBody().contains("Vacsora résztvevők"));
    assertFalse(content.htmlBody().contains("Egyágyas felár"));
    assertFalse(content.htmlBody().contains("Vacsora</td>"));
    assertFalse(content.htmlBody().contains("<script>"));
    assertTrue(content.htmlBody().contains("&lt;script&gt;szoba&lt;/script&gt;"));
  }

  @Test
  void createsMatchingAdminTemplateWithProtectedDetailButton() {
    NotificationEmailContent content = factory.admin(booking());

    assertTrue(content.plainTextBody().contains("Foglalás kezelése"));
    assertTrue(content.htmlBody().contains("class=\"action-button\""));
    assertTrue(
        content
            .htmlBody()
            .contains("href=\"https://example.com/admin/bookings/" + booking().id() + "\""));
    assertFalse(content.plainTextBody().contains("Egyágyas felár:"));
    assertFalse(content.plainTextBody().contains("Vacsora:"));
  }

  private NotificationBookingView booking() {
    return new NotificationBookingView(
        UUID.fromString("27a9a011-e917-44b9-bf75-9e9a476273f7"),
        "NB-1234567890ABCDEF",
        "Nisztor Panzió",
        LocalDate.of(2026, 9, 1),
        LocalDate.of(2026, 9, 3),
        2,
        2,
        0,
        0,
        2,
        0,
        new BigDecimal("520.00"),
        BigDecimal.ZERO,
        new BigDecimal("180.00"),
        BigDecimal.ZERO,
        new BigDecimal("700.00"),
        "RON",
        List.of(new NotificationRoomView("<script>szoba</script>", 1)));
  }

  private NotificationProperties properties() {
    return new NotificationProperties(
        true,
        5,
        60,
        Duration.ofSeconds(10),
        "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "https://example.com",
        "https://example.com",
        "no-reply@example.com",
        "Nisztor-Bukovina");
  }
}
