package com.bukovina.platform.support.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class NotificationMailSender {

  private final JavaMailSender mailSender;
  private final NotificationProperties properties;

  public NotificationMailSender(JavaMailSender mailSender, NotificationProperties properties) {
    this.mailSender = mailSender;
    this.properties = properties;
  }

  public void send(String recipient, String replyTo, NotificationEmailContent content)
      throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
    try {
      helper.setFrom(
          required(properties.fromAddress(), "MAIL_FROM_ADDRESS"),
          required(properties.fromName(), "MAIL_FROM_NAME"));
    } catch (UnsupportedEncodingException exception) {
      throw new MessagingException("Invalid sender name encoding", exception);
    }
    helper.setTo(recipient);
    if (replyTo != null) {
      helper.setReplyTo(replyTo);
    }
    helper.setSubject(content.subject());
    helper.setText(content.plainTextBody(), content.htmlBody());
    mailSender.send(message);
  }

  private String required(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(name + " is not configured");
    }
    return value.strip();
  }
}
