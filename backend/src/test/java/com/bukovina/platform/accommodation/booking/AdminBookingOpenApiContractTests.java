package com.bukovina.platform.accommodation.booking;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class AdminBookingOpenApiContractTests {

  @Test
  void documentsProtectedAdminBookingListFiltersPaginationAndDetail() throws IOException {
    String contract = Files.readString(Path.of("..", "docs", "api", "openapi.yaml"));
    Map<?, ?> document = new Yaml().load(contract);
    Map<?, ?> paths = (Map<?, ?>) document.get("paths");

    assertTrue(paths.containsKey("/admin/bookings"));
    assertTrue(paths.containsKey("/admin/bookings/{bookingId}"));
    assertTrue(paths.containsKey("/admin/bookings/{bookingId}/status"));
    assertTrue(paths.containsKey("/admin/bookings/{bookingId}/internal-note"));
    assertTrue(contract.contains("name: guesthouseId"));
    assertTrue(contract.contains("name: status"));
    assertTrue(contract.contains("name: createdFrom"));
    assertTrue(contract.contains("name: createdTo"));
    assertTrue(contract.contains("AdminBookingPage"));
    assertTrue(contract.contains("AdminBookingDetail"));
    assertTrue(contract.contains("UNDER_REVIEW"));
    assertTrue(contract.contains("INVALID_BOOKING_STATUS_TRANSITION"));
    assertTrue(contract.contains("INTERNAL_NOTE_TOO_LONG"));
    assertTrue(contract.contains("AdminBearerAuth"));
    assertFalse(contract.contains("managementTokenHash:"));
    assertFalse(contract.contains("idempotencyKeyHash:"));
    assertFalse(contract.contains("requestFingerprint:"));
  }
}
