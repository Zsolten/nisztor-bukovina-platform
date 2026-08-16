package com.bukovina.platform.tourism.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bukovina.platform.tourism.routing.DrivingDistanceProvider.AttractionPoint;
import com.bukovina.platform.tourism.routing.DrivingDistanceProvider.MatrixElement;
import com.sun.net.httpserver.HttpServer;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class GoogleRoutesDrivingDistanceProviderTests {

  @Test
  void sendsTheGoogleMatrixRequestAndParsesStreamingElements() throws Exception {
    AtomicReference<String> requestBody = new AtomicReference<>();
    AtomicReference<String> apiKey = new AtomicReference<>();
    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/distanceMatrix/v2:computeRouteMatrix",
        exchange -> {
          apiKey.set(exchange.getRequestHeaders().getFirst("X-Goog-Api-Key"));
          requestBody.set(
              new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          byte[] body =
              ("""
                  {"originIndex":0,"destinationIndex":0,"condition":"ROUTE_EXISTS","distanceMeters":12345,"duration":"678.4s"}
                  {"originIndex":0,"destinationIndex":1,"condition":"ROUTE_NOT_FOUND","status":{"message":"No route"}}
                  """)
                  .getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    server.start();
    try {
      GoogleRoutesDrivingDistanceProvider provider =
          new GoogleRoutesDrivingDistanceProvider(
              new GoogleRoutesProperties(
                  "test-key", "http://127.0.0.1:" + server.getAddress().getPort()));
      List<MatrixElement> result =
          provider.calculate(
              List.of(point("00000000-0000-0000-0000-000000000001", "45.1", "23.1")),
              List.of(
                  point("00000000-0000-0000-0000-000000000002", "45.2", "23.2"),
                  point("00000000-0000-0000-0000-000000000003", "45.3", "23.3")));

      assertEquals("test-key", apiKey.get());
      assertTrue(requestBody.get().contains("\"travelMode\":\"DRIVE\""));
      assertTrue(requestBody.get().contains("\"routingPreference\":\"TRAFFIC_UNAWARE\""));
      assertTrue(
          requestBody
              .get()
              .contains("\"location\":{\"latLng\":{\"latitude\":45.1,\"longitude\":23.1}}"));
      assertEquals(2, result.size());
      assertEquals(12_345, result.getFirst().distanceMeters());
      assertEquals(678, result.getFirst().durationSeconds());
      assertEquals("No route", result.get(1).failureReason());
    } finally {
      server.stop(0);
    }
  }

  private static AttractionPoint point(String id, String latitude, String longitude) {
    return new AttractionPoint(
        UUID.fromString(id), new BigDecimal(latitude), new BigDecimal(longitude));
  }
}
