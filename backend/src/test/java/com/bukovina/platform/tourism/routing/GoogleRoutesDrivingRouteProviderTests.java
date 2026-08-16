package com.bukovina.platform.tourism.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RouteLeg;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RoutePoint;
import com.sun.net.httpserver.HttpServer;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class GoogleRoutesDrivingRouteProviderTests {

  @Test
  void sendsTheGoogleRoutesRequestAndParsesRouteLegs() throws Exception {
    AtomicReference<String> requestBody = new AtomicReference<>();
    AtomicReference<String> fieldMask = new AtomicReference<>();
    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/directions/v2:computeRoutes",
        exchange -> {
          requestBody.set(
              new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          fieldMask.set(exchange.getRequestHeaders().getFirst("X-Goog-FieldMask"));
          byte[] body =
              """
                  {"routes":[{"legs":[
                    {"distanceMeters":12345,"duration":"678.4s","polyline":{"encodedPolyline":"abc"}},
                    {"distanceMeters":321,"duration":"4s","polyline":{"encodedPolyline":"def"}}
                  ]}]}
                  """
                  .getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    server.start();
    try {
      GoogleRoutesDrivingRouteProvider provider =
          new GoogleRoutesDrivingRouteProvider(
              new GoogleRoutesProperties(
                  "test-key", "http://127.0.0.1:" + server.getAddress().getPort()));

      List<RouteLeg> result =
          provider.calculate(
              point("45.1", "23.1"), List.of(point("45.2", "23.2")), point("45.3", "23.3"));

      assertTrue(requestBody.get().contains("\"travelMode\":\"DRIVE\""));
      assertTrue(requestBody.get().contains("\"polylineQuality\":\"OVERVIEW\""));
      assertTrue(
          requestBody
              .get()
              .contains("\"location\":{\"latLng\":{\"latitude\":45.1,\"longitude\":23.1}}"));
      assertEquals(
          "routes.legs.distanceMeters,routes.legs.duration,routes.legs.polyline.encodedPolyline",
          fieldMask.get());
      assertEquals(2, result.size());
      assertEquals(12_345, result.getFirst().distanceMeters());
      assertEquals(678, result.getFirst().durationSeconds());
      assertEquals("def", result.get(1).encodedPolyline());
    } finally {
      server.stop(0);
    }
  }

  private static RoutePoint point(String latitude, String longitude) {
    return new RoutePoint(new BigDecimal(latitude), new BigDecimal(longitude));
  }
}
