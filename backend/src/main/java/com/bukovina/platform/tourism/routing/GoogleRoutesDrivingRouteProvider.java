package com.bukovina.platform.tourism.routing;

import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RouteLeg;
import com.bukovina.platform.tourism.routing.DrivingRouteProvider.RoutePoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
class GoogleRoutesDrivingRouteProvider implements DrivingRouteProvider {

  private static final String FIELD_MASK =
      "routes.legs.distanceMeters,routes.legs.duration,routes.legs.polyline.encodedPolyline";
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(20);
  private final GoogleRoutesProperties properties;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final RestClient restClient;

  GoogleRoutesDrivingRouteProvider(GoogleRoutesProperties properties) {
    this.properties = properties;
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
    requestFactory.setReadTimeout(READ_TIMEOUT);
    restClient =
        RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(requestFactory).build();
  }

  @Override
  public List<RouteLeg> calculate(
      RoutePoint origin, List<RoutePoint> intermediates, RoutePoint destination) {
    if (properties.apiKey() == null || properties.apiKey().isBlank()) {
      throw new IllegalStateException("GOOGLE_ROUTES_API_KEY_NOT_CONFIGURED");
    }
    String response =
        restClient
            .post()
            .uri("/directions/v2:computeRoutes")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Goog-Api-Key", properties.apiKey())
            .header("X-Goog-FieldMask", FIELD_MASK)
            .body(
                new RouteRequest(
                    waypoint(origin),
                    waypoint(destination),
                    intermediates.stream().map(GoogleRoutesDrivingRouteProvider::waypoint).toList(),
                    "DRIVE",
                    "TRAFFIC_UNAWARE",
                    "OVERVIEW",
                    "ENCODED_POLYLINE"))
            .retrieve()
            .body(String.class);
    return parse(response);
  }

  private List<RouteLeg> parse(String response) {
    try {
      GoogleRoutesResponse parsed = objectMapper.readValue(response, GoogleRoutesResponse.class);
      if (parsed.routes() == null
          || parsed.routes().size() != 1
          || parsed.routes().getFirst().legs() == null) {
        throw new IllegalStateException("GOOGLE_ROUTES_INVALID_RESPONSE");
      }
      return parsed.routes().getFirst().legs().stream()
          .map(
              leg -> {
                if (leg.distanceMeters() == null
                    || leg.duration() == null
                    || leg.polyline() == null
                    || leg.polyline().encodedPolyline() == null
                    || leg.polyline().encodedPolyline().isBlank()) {
                  throw new IllegalStateException("GOOGLE_ROUTES_INVALID_RESPONSE");
                }
                return new RouteLeg(
                    leg.distanceMeters(),
                    durationSeconds(leg.duration()),
                    leg.polyline().encodedPolyline());
              })
          .toList();
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("GOOGLE_ROUTES_INVALID_RESPONSE", exception);
    }
  }

  private static int durationSeconds(String duration) {
    if (!duration.endsWith("s")) {
      throw new IllegalArgumentException("GOOGLE_ROUTES_INVALID_DURATION");
    }
    return new BigDecimal(duration.substring(0, duration.length() - 1))
        .setScale(0, RoundingMode.HALF_UP)
        .intValueExact();
  }

  private static Waypoint waypoint(RoutePoint point) {
    return new Waypoint(new Location(new LatLng(point.latitude(), point.longitude())));
  }

  private record RouteRequest(
      Waypoint origin,
      Waypoint destination,
      List<Waypoint> intermediates,
      String travelMode,
      String routingPreference,
      String polylineQuality,
      String polylineEncoding) {}

  private record Waypoint(Location location) {}

  private record Location(LatLng latLng) {}

  private record LatLng(BigDecimal latitude, BigDecimal longitude) {}

  private record GoogleRoutesResponse(List<GoogleRoute> routes) {}

  private record GoogleRoute(List<GoogleRouteLeg> legs) {}

  private record GoogleRouteLeg(Integer distanceMeters, String duration, GooglePolyline polyline) {}

  private record GooglePolyline(String encodedPolyline) {}
}
