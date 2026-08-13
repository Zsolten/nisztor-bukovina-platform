package com.bukovina.platform.accommodation.booking;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class BookingOpenApiContractTests {

  @Test
  void documentsPublicQuoteSubmissionErrorsAndIdempotency() throws IOException {
    String contract = Files.readString(Path.of("..", "docs", "api", "openapi.yaml"));
    Map<?, ?> document = new Yaml().load(contract);
    Map<?, ?> paths = (Map<?, ?>) document.get("paths");

    assertTrue(paths.containsKey("/booking-quotes"));
    assertTrue(paths.containsKey("/booking-requests"));
    assertTrue(paths.containsKey("/booking-management/{token}"));
    assertTrue(paths.containsKey("/booking-management/{token}/cancellation"));
    assertTrue(contract.contains("name: Idempotency-Key"));
    assertTrue(contract.contains("BookingValidationError"));
    assertTrue(contract.contains("acceptedTotal:"));
    assertTrue(contract.contains("accommodationTotal:"));
    assertTrue(contract.contains("adultAccommodationTotal:"));
    assertTrue(contract.contains("childAccommodationTotal:"));
    assertFalse(contract.contains("accommodationTaxRate:"));
    assertFalse(contract.contains("cityTaxAmount:"));
    assertTrue(contract.contains("enum: [RECEIVED]"));
    assertFalse(contract.contains("PENDING_EMAIL_VERIFICATION"));
    assertTrue(contract.contains("BOOKING_MANAGEMENT_LINK_INVALID"));
    assertTrue(contract.contains("token was revoked"));
  }
}
