package com.bukovina.platform.support.notification;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class BookingNotificationEmailFactory {

  private static final String FONT_STACK = "'Avenir Next',Avenir,'Segoe UI',Arial,sans-serif";
  private static final String SERIF_STACK =
      "Iowan Old Style,Baskerville,'Palatino Linotype',Georgia,serif";
  private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

  private final NotificationProperties properties;

  public BookingNotificationEmailFactory(NotificationProperties properties) {
    this.properties = properties;
  }

  public NotificationEmailContent guest(
      NotificationBookingView booking, String language, String rawManagementToken, String replyTo) {
    GuestCopy copy = guestCopy(language);
    String managementUrl =
        baseUrl(properties.publicBaseUrl())
            + "/"
            + language
            + "/booking-management/"
            + rawManagementToken;
    List<SummaryRow> rows = summaryRows(booking, copy.labels());
    String contactText = copy.contactText().formatted(replyTo);
    String greeting = copy.greeting().formatted(booking.contactName());
    return new NotificationEmailContent(
        copy.subject().formatted(booking.publicReference()),
        plainText(
            copy.title(),
            greeting,
            copy.intro(),
            copy.labels().summaryTitle(),
            rows,
            copy.button(),
            managementUrl,
            contactText),
        html(
            language,
            copy.eyebrow(),
            copy.title(),
            greeting,
            copy.intro(),
            booking.publicReference(),
            copy.labels().reference(),
            copy.labels().summaryTitle(),
            rows,
            copy.button(),
            managementUrl,
            contactText));
  }

  public NotificationEmailContent admin(NotificationBookingView booking) {
    String adminUrl =
        baseUrl(properties.adminBaseUrl()) + "/admin/bookings/" + booking.id().toString();
    Labels labels = labelsHu();
    List<SummaryRow> rows = summaryRows(booking, labels);
    String title = "Új foglalási kérelem érkezett";
    String intro =
        "A kérelem ellenőrzésre vár. A link megnyitása önmagában nem módosítja a foglalás állapotát.";
    return new NotificationEmailContent(
        "Új foglalási kérelem – " + booking.publicReference(),
        plainText(
            title, null, intro, labels.summaryTitle(), rows, "Foglalás kezelése", adminUrl, null),
        html(
            "hu",
            "Új foglalási kérelem",
            title,
            null,
            intro,
            booking.publicReference(),
            labels.reference(),
            labels.summaryTitle(),
            rows,
            "Foglalás kezelése",
            adminUrl,
            null));
  }

  public NotificationEmailContent decision(
      NotificationBookingView booking,
      NotificationType notificationType,
      String language,
      String guestMessage,
      String replyTo) {
    boolean confirmed = notificationType == NotificationType.BOOKING_CONFIRMED_GUEST;
    if (!confirmed && notificationType != NotificationType.BOOKING_REJECTED_GUEST) {
      throw new IllegalArgumentException("Unsupported guest decision notification type");
    }
    DecisionCopy copy = decisionCopy(language, confirmed);
    String intro = copy.intro();
    if (guestMessage != null) {
      intro += " " + copy.messageLabel() + ": „" + guestMessage + "”";
    }
    List<SummaryRow> rows =
        confirmed
            ? confirmationSummaryRows(booking, copy.labels())
            : summaryRows(booking, copy.labels());
    String contactUrl = "mailto:" + replyTo;
    String contactText = copy.contactText().formatted(replyTo);
    return new NotificationEmailContent(
        copy.subject().formatted(booking.publicReference()),
        plainText(
            copy.title(),
            copy.greeting().formatted(booking.contactName()),
            intro,
            copy.labels().summaryTitle(),
            rows,
            copy.button(),
            contactUrl,
            contactText),
        html(
            language,
            copy.eyebrow(),
            copy.title(),
            copy.greeting().formatted(booking.contactName()),
            intro,
            booking.publicReference(),
            copy.labels().reference(),
            copy.labels().summaryTitle(),
            rows,
            copy.button(),
            contactUrl,
            contactText));
  }

  private List<SummaryRow> summaryRows(NotificationBookingView booking, Labels labels) {
    List<SummaryRow> rows = new ArrayList<>();
    rows.add(new SummaryRow(labels.reference(), booking.publicReference(), false));
    rows.add(new SummaryRow(labels.guesthouse(), booking.guesthouseName(), false));
    rows.add(
        new SummaryRow(
            labels.stay(),
            formatDate(booking.checkInDate()) + " – " + formatDate(booking.checkOutDate()),
            false));
    rows.add(new SummaryRow(labels.nights(), Long.toString(booking.nights()), false));
    addCount(rows, labels.adults(), booking.adults());
    addCount(rows, labels.children3to10(), booking.childrenAge3to10());
    addCount(rows, labels.children0to3(), booking.childrenAge0to3());
    rows.add(new SummaryRow(labels.rooms(), rooms(booking), false));
    addCount(rows, labels.breakfastParticipants(), booking.breakfastParticipants());
    addCount(rows, labels.dinnerParticipants(), booking.dinnerParticipants());
    addMoney(rows, labels.accommodation(), booking.accommodationTotal(), booking.currency(), false);
    addMoney(
        rows,
        labels.singleRoomSurcharge(),
        booking.singleRoomSurcharge(),
        booking.currency(),
        false);
    addMoney(rows, labels.breakfast(), booking.breakfastTotal(), booking.currency(), false);
    addMoney(rows, labels.dinner(), booking.dinnerTotal(), booking.currency(), false);
    addMoney(rows, labels.total(), booking.totalPayable(), booking.currency(), true);
    return rows;
  }

  private List<SummaryRow> confirmationSummaryRows(NotificationBookingView booking, Labels labels) {
    return List.of(
        new SummaryRow(labels.reference(), booking.publicReference(), false),
        new SummaryRow(
            labels.stay(),
            formatDate(booking.checkInDate()) + " – " + formatDate(booking.checkOutDate()),
            false));
  }

  private void addCount(List<SummaryRow> rows, String label, int value) {
    if (value > 0) {
      rows.add(new SummaryRow(label, Integer.toString(value), false));
    }
  }

  private void addMoney(
      List<SummaryRow> rows, String label, BigDecimal value, String currency, boolean emphasized) {
    if (value.signum() > 0) {
      rows.add(
          new SummaryRow(
              label, value.stripTrailingZeros().toPlainString() + " " + currency, emphasized));
    }
  }

  private String plainText(
      String title,
      String greeting,
      String intro,
      String summaryTitle,
      List<SummaryRow> rows,
      String button,
      String url,
      String footer) {
    StringBuilder body = new StringBuilder(title).append("\n\n");
    if (greeting != null) {
      body.append(greeting).append("\n\n");
    }
    body.append(intro).append("\n\n").append(summaryTitle).append("\n");
    rows.forEach(row -> body.append(row.label()).append(": ").append(row.value()).append('\n'));
    body.append("\n").append(button).append(":\n").append(url);
    if (footer != null) {
      body.append("\n\n").append(footer);
    }
    return body.toString();
  }

  private String html(
      String language,
      String eyebrow,
      String title,
      String greeting,
      String intro,
      String reference,
      String referenceLabel,
      String summaryTitle,
      List<SummaryRow> rows,
      String button,
      String url,
      String footer) {
    return """
        <!doctype html>
        <html lang="%s">
          <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>
              @media only screen and (max-width:620px) {
                .email-shell { padding: 8px 4px !important; }
                .email-card { box-sizing: border-box !important; width: 100%% !important; max-width: 100%% !important; padding: 26px 12px !important; }
                .email-title { font-size: 28px !important; }
                .summary-table { box-sizing: border-box !important; width: 100%% !important; max-width: 100%% !important; table-layout: fixed !important; }
                .summary-cell { box-sizing: border-box !important; display: table-cell !important; }
                .summary-label { width: 42%% !important; padding: 9px 5px 9px 10px !important; text-align: left !important; }
                .summary-value { width: 58%% !important; padding: 9px 10px 9px 5px !important; font-size: 12px !important; line-height: 1.35 !important; text-align: right !important; overflow-wrap: anywhere !important; word-break: normal !important; }
                .action-button { box-sizing: border-box !important; display: block !important; width: 100%% !important; padding-right: 12px !important; padding-left: 12px !important; text-align: center !important; }
                .reference-value { overflow-wrap: anywhere !important; word-break: break-word !important; }
              }
            </style>
          </head>
          <body style="width:100%%;margin:0;background:#f4efe4;color:#27241f;font-family:%s;-webkit-text-size-adjust:100%%;">
            <div class="email-shell" style="padding:32px 12px;background:#f4efe4;">
              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="width:100%%;table-layout:fixed;">
                <tr><td align="center">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="width:100%%;max-width:620px;background:#fbf8f1;border:1px solid #d8d0c3;">
                    <tr><td style="height:7px;background:#29493a;font-size:0;line-height:0;">&nbsp;</td></tr>
                    <tr><td class="email-card" style="box-sizing:border-box;padding:42px 46px;">
                      <p style="margin:0 0 10px;color:#c8984c;font-size:12px;font-weight:700;letter-spacing:1.3px;text-align:center;text-transform:uppercase;">%s</p>
                      <h1 class="email-title" style="margin:0;color:#27241f;font-family:%s;font-size:34px;font-weight:500;line-height:1.18;text-align:center;">%s</h1>
                      %s
                      <p style="margin:16px auto 0;max-width:500px;color:#6e665b;font-size:15px;line-height:1.65;text-align:center;">%s</p>
                      <div style="margin:26px 0;padding:15px;border-top:1px solid #d8d0c3;border-bottom:1px solid #d8d0c3;text-align:center;">
                        <span style="display:block;color:#6e665b;font-size:11px;font-weight:700;letter-spacing:1px;text-transform:uppercase;">%s</span>
                        <strong class="reference-value" style="display:block;margin-top:5px;font-family:%s;font-size:20px;font-weight:500;letter-spacing:.6px;">%s</strong>
                      </div>
                      <table class="summary-table" role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="box-sizing:border-box;width:100%%;max-width:100%%;table-layout:fixed;border:1px solid #d8d0c3;">
                        <tr><td colspan="2" style="padding:16px 18px 10px;color:#29493a;font-size:13px;font-weight:700;">%s</td></tr>
                        %s
                      </table>
                      <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                        <tr><td align="center" style="padding:28px 0 8px;">
                          <a class="action-button" href="%s" style="display:inline-block;padding:15px 28px;background:#29493a;color:#ffffff;font-size:15px;font-weight:700;text-decoration:none;">%s</a>
                        </td></tr>
                      </table>
                      %s
                    </td></tr>
                    <tr><td style="padding:18px 24px;background:#193027;color:#e8e1d5;font-size:12px;line-height:1.5;text-align:center;">Nisztor–Bukovina Platform</td></tr>
                  </table>
                </td></tr>
              </table>
            </div>
          </body>
        </html>
        """
        .formatted(
            escape(language),
            FONT_STACK,
            escape(eyebrow),
            SERIF_STACK,
            escape(title),
            greetingHtml(greeting),
            escape(intro),
            escape(referenceLabel),
            SERIF_STACK,
            escape(reference),
            escape(summaryTitle),
            htmlRows(rows),
            escape(url),
            escape(button),
            footerHtml(footer));
  }

  private String htmlRows(List<SummaryRow> rows) {
    StringBuilder result = new StringBuilder();
    for (SummaryRow row : rows) {
      String border = row.emphasized() ? "border-top:1px solid #c8984c;" : "";
      String labelClass = row.emphasized() ? " summary-total-label" : "";
      String valueClass = row.emphasized() ? " summary-total-value" : "";
      String valueStyle =
          row.emphasized()
              ? "font-family:" + SERIF_STACK + ";font-size:19px;font-weight:600;"
              : "font-size:14px;font-weight:600;";
      result.append(
          """
          <tr>
            <td class="summary-cell summary-label%s" width="42%%" style="box-sizing:border-box;%s;padding:8px 9px 8px 18px;color:#6e665b;font-size:13px;overflow-wrap:anywhere;">%s</td>
            <td class="summary-cell summary-value%s" width="58%%" style="box-sizing:border-box;%s;padding:8px 18px 8px 9px;color:#27241f;%s;text-align:right;overflow-wrap:anywhere;word-break:break-word;">%s</td>
          </tr>
          """
              .formatted(
                  labelClass,
                  border,
                  escape(row.label()),
                  valueClass,
                  border,
                  valueStyle,
                  htmlValue(row.value())));
    }
    return result.toString();
  }

  private String footerHtml(String footer) {
    if (footer == null) {
      return "";
    }
    return "<p style=\"margin:16px 0 0;color:#6e665b;font-size:13px;line-height:1.55;text-align:center;\">"
        + escape(footer)
        + "</p>";
  }

  private String greetingHtml(String greeting) {
    if (greeting == null) {
      return "";
    }
    return "<p style=\"margin:20px 0 0;color:#29493a;font-size:16px;font-weight:700;line-height:1.5;text-align:center;\">"
        + escape(greeting)
        + "</p>";
  }

  private String rooms(NotificationBookingView booking) {
    return booking.rooms().stream()
        .filter(room -> room.quantity() > 0)
        .map(room -> room.quantity() + " × " + room.name())
        .reduce((left, right) -> left + "\n" + right)
        .orElse("-");
  }

  private String formatDate(java.time.LocalDate date) {
    return date.format(COMPACT_DATE);
  }

  private String htmlValue(String value) {
    return escape(value).replace("\n", "<br>");
  }

  private GuestCopy guestCopy(String language) {
    return switch (language) {
      case "ro" ->
          new GuestCopy(
              "Am primit cererea de rezervare %s",
              "Cerere primită",
              "Am primit cererea dumneavoastră",
              "Bună, %s!",
              "Aceasta nu este încă o rezervare confirmată; așteaptă aprobarea pensiunii.",
              "Gestionați rezervarea",
              "Dacă doriți să ne scrieți, răspundeți la acest e-mail. Mesajul va fi trimis la %s.",
              labelsRo());
      case "en" ->
          new GuestCopy(
              "We received booking request %s",
              "Request received",
              "We received your booking request",
              "Dear %s,",
              "This is not yet a confirmed reservation; it is awaiting guesthouse approval.",
              "Manage booking",
              "To contact us, reply to this email. Your message will be sent to %s.",
              labelsEn());
      default ->
          new GuestCopy(
              "Megkaptuk foglalási kérelmét – %s",
              "Kérelem megérkezett",
              "Megkaptuk foglalási kérelmét",
              "Kedves %s!",
              "Ez még nem visszaigazolt foglalás; a panzió jóváhagyására vár.",
              "Foglalás kezelése",
              "Ha üzenetet küldene nekünk, válaszoljon erre a levélre. A válasz a(z) %s címre érkezik.",
              labelsHu());
    };
  }

  private DecisionCopy decisionCopy(String language, boolean confirmed) {
    return switch (language) {
      case "ro" ->
          confirmed
              ? new DecisionCopy(
                  "Cererea de rezervare %s a fost confirmată",
                  "Rezervare confirmată",
                  "Cererea dumneavoastră a fost confirmată",
                  "Bună, %s!",
                  "Pensiunea a confirmat cererea dumneavoastră de rezervare. Vă așteptăm cu drag!",
                  "Mesajul pensiunii",
                  "Scrieți pensiunii",
                  "Dacă aveți întrebări, răspundeți la acest e-mail. Mesajul va fi trimis la %s.",
                  labelsRo())
              : new DecisionCopy(
                  "Cererea de rezervare %s a fost respinsă",
                  "Cerere respinsă",
                  "Cererea dumneavoastră nu a putut fi confirmată",
                  "Bună, %s!",
                  "Pensiunea a respins cererea dumneavoastră de rezervare.",
                  "Mesajul pensiunii",
                  "Scrieți pensiunii",
                  "Dacă aveți întrebări, răspundeți la acest e-mail. Mesajul va fi trimis la %s.",
                  labelsRo());
      case "en" ->
          confirmed
              ? new DecisionCopy(
                  "Booking request %s confirmed",
                  "Booking confirmed",
                  "Your booking request has been confirmed",
                  "Dear %s,",
                  "The guesthouse has confirmed your booking request. We look forward to welcoming you!",
                  "Message from the guesthouse",
                  "Contact guesthouse",
                  "If you have any questions, reply to this email. Your message will be sent to %s.",
                  labelsEn())
              : new DecisionCopy(
                  "Booking request %s declined",
                  "Request declined",
                  "Your booking request could not be confirmed",
                  "Dear %s,",
                  "The guesthouse has declined your booking request.",
                  "Message from the guesthouse",
                  "Contact guesthouse",
                  "If you have any questions, reply to this email. Your message will be sent to %s.",
                  labelsEn());
      default ->
          confirmed
              ? new DecisionCopy(
                  "Foglalási kérelme visszaigazolva – %s",
                  "Foglalás visszaigazolva",
                  "Foglalási kérelmét visszaigazoltuk",
                  "Kedves %s!",
                  "A panzió visszaigazolta foglalási kérelmét. Szeretettel várjuk!",
                  "A panzió üzenete",
                  "Üzenet a panziónak",
                  "Ha kérdése van, válaszoljon erre a levélre. A válasz a(z) %s címre érkezik.",
                  labelsHu())
              : new DecisionCopy(
                  "Foglalási kérelme elutasítva – %s",
                  "Kérelem elutasítva",
                  "Foglalási kérelmét nem tudtuk visszaigazolni",
                  "Kedves %s!",
                  "A panzió elutasította foglalási kérelmét.",
                  "A panzió üzenete",
                  "Üzenet a panziónak",
                  "Ha kérdése van, válaszoljon erre a levélre. A válasz a(z) %s címre érkezik.",
                  labelsHu());
    };
  }

  private Labels labelsHu() {
    return new Labels(
        Locale.forLanguageTag("hu-HU"),
        "Foglalási összefoglaló",
        "Azonosító",
        "Panzió",
        "Időszak",
        "Éjszakák",
        "Felnőttek",
        "Gyermekek (3–10 év)",
        "Gyermekek (0–3 év)",
        "Szobák",
        "Reggeli résztvevők",
        "Vacsora résztvevők",
        "Szállás",
        "Egyágyas felár",
        "Reggeli",
        "Vacsora",
        "Végösszeg");
  }

  private Labels labelsRo() {
    return new Labels(
        Locale.forLanguageTag("ro-RO"),
        "Sumarul rezervării",
        "Referință",
        "Pensiune",
        "Perioadă",
        "Nopți",
        "Adulți",
        "Copii (3–10 ani)",
        "Copii (0–3 ani)",
        "Camere",
        "Participanți la mic dejun",
        "Participanți la cină",
        "Cazare",
        "Supliment cameră single",
        "Mic dejun",
        "Cină",
        "Total");
  }

  private Labels labelsEn() {
    return new Labels(
        Locale.forLanguageTag("en-GB"),
        "Booking summary",
        "Reference",
        "Guesthouse",
        "Stay",
        "Nights",
        "Adults",
        "Children (3–10)",
        "Children (0–3)",
        "Rooms",
        "Breakfast participants",
        "Dinner participants",
        "Accommodation",
        "Single-room surcharge",
        "Breakfast",
        "Dinner",
        "Total");
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

  private String escape(String value) {
    return HtmlUtils.htmlEscape(value == null ? "" : value);
  }

  private record SummaryRow(String label, String value, boolean emphasized) {}

  private record GuestCopy(
      String subject,
      String eyebrow,
      String title,
      String greeting,
      String intro,
      String button,
      String contactText,
      Labels labels) {}

  private record DecisionCopy(
      String subject,
      String eyebrow,
      String title,
      String greeting,
      String intro,
      String messageLabel,
      String button,
      String contactText,
      Labels labels) {}

  private record Labels(
      Locale locale,
      String summaryTitle,
      String reference,
      String guesthouse,
      String stay,
      String nights,
      String adults,
      String children3to10,
      String children0to3,
      String rooms,
      String breakfastParticipants,
      String dinnerParticipants,
      String accommodation,
      String singleRoomSurcharge,
      String breakfast,
      String dinner,
      String total) {}
}
