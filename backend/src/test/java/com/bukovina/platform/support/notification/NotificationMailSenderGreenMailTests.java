package com.bukovina.platform.support.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class NotificationMailSenderGreenMailTests {

  private GreenMail greenMail;
  private NotificationMailSender sender;

  @BeforeEach
  void startSmtpServer() {
    greenMail = new GreenMail(new ServerSetup(0, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));
    greenMail.start();
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
    javaMailSender.setHost("127.0.0.1");
    javaMailSender.setPort(greenMail.getSmtp().getPort());
    sender = new NotificationMailSender(javaMailSender, properties());
  }

  @AfterEach
  void stopSmtpServer() {
    greenMail.stop();
  }

  @Test
  void sendsHtmlAndPlainTextAlternativesWithConfiguredGuesthouseReplyTo() throws Exception {
    sender.send(
        "guest@example.com",
        "guesthouse@example.com",
        new NotificationEmailContent(
            "Foglalási kérelem",
            "Árvíztűrő tükörfúrógép",
            "<html><body><strong>Árvíztűrő tükörfúrógép</strong></body></html>"));

    assertTrue(greenMail.waitForIncomingEmail(1));
    MimeMessage message = greenMail.getReceivedMessages()[0];
    assertEquals("Foglalási kérelem", message.getSubject());
    assertTrue(findContent(message, "text/plain").contains("Árvíztűrő tükörfúrógép"));
    assertTrue(findContent(message, "text/html").contains("<strong>"));
    assertEquals("guest@example.com", message.getAllRecipients()[0].toString());
    assertEquals("guesthouse@example.com", message.getReplyTo()[0].toString());
  }

  private String findContent(Part part, String mimeType) throws Exception {
    if (part.isMimeType(mimeType)) {
      return part.getContent().toString();
    }
    if (part.getContent() instanceof Multipart multipart) {
      for (int index = 0; index < multipart.getCount(); index++) {
        BodyPart bodyPart = multipart.getBodyPart(index);
        String content = findContent(bodyPart, mimeType);
        if (!content.isEmpty()) {
          return content;
        }
      }
    }
    return "";
  }

  private NotificationProperties properties() {
    return new NotificationProperties(
        true,
        5,
        60,
        Duration.ofSeconds(10),
        "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "http://localhost:5173",
        "http://localhost:5173",
        "sender@example.com",
        "Nisztor-Bukovina");
  }
}
