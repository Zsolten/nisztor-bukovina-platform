package com.bukovina.platform.support.notification;

import org.springframework.stereotype.Component;

@Component
public class BookingNotificationEmailFactory {

  private final NotificationProperties properties;

  public BookingNotificationEmailFactory(NotificationProperties properties) {
    this.properties = properties;
  }

  public NotificationEmailContent guest(
      NotificationBookingView booking, String language, String rawManagementToken) {
    String managementUrl =
        baseUrl(properties.publicBaseUrl())
            + "/"
            + language
            + "/booking-management/"
            + rawManagementToken;
    return switch (language) {
      case "ro" ->
          new NotificationEmailContent(
              "Am primit cererea de rezervare " + booking.publicReference(),
              guestBodyRo(booking, managementUrl));
      case "en" ->
          new NotificationEmailContent(
              "We received booking request " + booking.publicReference(),
              guestBodyEn(booking, managementUrl));
      default ->
          new NotificationEmailContent(
              "Megkaptuk foglalási kérelmét – " + booking.publicReference(),
              guestBodyHu(booking, managementUrl));
    };
  }

  public NotificationEmailContent admin(NotificationBookingView booking) {
    String adminUrl =
        baseUrl(properties.adminBaseUrl()) + "/admin/bookings/" + booking.id().toString();
    String body =
        """
        Új foglalási kérelem érkezett.

        Azonosító: %s
        Panzió: %s
        Időszak: %s – %s (%d éjszaka)
        Vendégek száma: %d
        Szobák: %s
        Végösszeg: %s %s

        A foglalás védett adminoldala:
        %s

        A link megnyitása önmagában nem módosítja a foglalás állapotát.
        """
            .formatted(
                booking.publicReference(),
                booking.guesthouseName(),
                booking.checkInDate(),
                booking.checkOutDate(),
                booking.nights(),
                booking.totalGuests(),
                rooms(booking),
                booking.totalPayable().toPlainString(),
                booking.currency(),
                adminUrl);
    return new NotificationEmailContent(
        "Új foglalási kérelem – " + booking.publicReference(), body);
  }

  private String guestBodyHu(NotificationBookingView booking, String managementUrl) {
    return """
        Kedves Vendégünk!

        Megkaptuk foglalási kérelmét. Ez még nem visszaigazolt foglalás; a panzió jóváhagyására vár.

        Azonosító: %s
        Panzió: %s
        Időszak: %s – %s (%d éjszaka)
        Vendégek: %d felnőtt, %d gyermek (3–10 év), %d gyermek (0–3 év)
        Szobák: %s
        Reggeli résztvevők: %d
        Vacsora résztvevők: %d
        Szállás: %s %s
        Egyágyas felár: %s %s
        Reggeli: %s %s
        Vacsora: %s %s
        Végösszeg: %s %s

        Foglalási kérelmének biztonságos kezelése:
        %s
        """
        .formatted(guestValues(booking, managementUrl));
  }

  private String guestBodyRo(NotificationBookingView booking, String managementUrl) {
    return """
        Stimate oaspete,

        Am primit cererea dumneavoastră. Aceasta nu este încă o rezervare confirmată; așteaptă aprobarea pensiunii.

        Referință: %s
        Pensiune: %s
        Perioadă: %s – %s (%d nopți)
        Oaspeți: %d adulți, %d copii (3–10 ani), %d copii (0–3 ani)
        Camere: %s
        Participanți la mic dejun: %d
        Participanți la cină: %d
        Cazare: %s %s
        Supliment cameră single: %s %s
        Mic dejun: %s %s
        Cină: %s %s
        Total: %s %s

        Gestionați în siguranță cererea:
        %s
        """
        .formatted(guestValues(booking, managementUrl));
  }

  private String guestBodyEn(NotificationBookingView booking, String managementUrl) {
    return """
        Dear Guest,

        We received your booking request. This is not yet a confirmed reservation; it is awaiting guesthouse approval.

        Reference: %s
        Guesthouse: %s
        Stay: %s – %s (%d nights)
        Guests: %d adults, %d children (3–10), %d children (0–3)
        Rooms: %s
        Breakfast participants: %d
        Dinner participants: %d
        Accommodation: %s %s
        Single-room surcharge: %s %s
        Breakfast: %s %s
        Dinner: %s %s
        Total: %s %s

        Securely manage your request:
        %s
        """
        .formatted(guestValues(booking, managementUrl));
  }

  private Object[] guestValues(NotificationBookingView booking, String managementUrl) {
    return new Object[] {
      booking.publicReference(),
      booking.guesthouseName(),
      booking.checkInDate(),
      booking.checkOutDate(),
      booking.nights(),
      booking.adults(),
      booking.childrenAge3to10(),
      booking.childrenAge0to3(),
      rooms(booking),
      booking.breakfastParticipants(),
      booking.dinnerParticipants(),
      booking.accommodationTotal().toPlainString(),
      booking.currency(),
      booking.singleRoomSurcharge().toPlainString(),
      booking.currency(),
      booking.breakfastTotal().toPlainString(),
      booking.currency(),
      booking.dinnerTotal().toPlainString(),
      booking.currency(),
      booking.totalPayable().toPlainString(),
      booking.currency(),
      managementUrl
    };
  }

  private String rooms(NotificationBookingView booking) {
    return booking.rooms().stream()
        .map(room -> room.quantity() + " × " + room.name())
        .reduce((left, right) -> left + ", " + right)
        .orElse("-");
  }

  private String baseUrl(String value) {
    String normalized = requireConfigured(value, "application base URL");
    while (normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  private String requireConfigured(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(name + " is not configured");
    }
    return value.strip();
  }
}
