package com.bukovina.platform.tourism.routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
class GoogleRoutesDrivingDistanceProvider implements DrivingDistanceProvider {

  private static final String FIELD_MASK =
      "originIndex,destinationIndex,condition,distanceMeters,duration,status";
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(20);
  private final GoogleRoutesProperties properties;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final RestClient restClient;

  GoogleRoutesDrivingDistanceProvider(GoogleRoutesProperties properties) {
    this.properties = properties;
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
    requestFactory.setReadTimeout(READ_TIMEOUT);
    this.restClient =
        RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(requestFactory).build();
  }

  @Override
  public List<MatrixElement> calculate(
      List<AttractionPoint> origins, List<AttractionPoint> destinations) {
    if (properties.apiKey() == null || properties.apiKey().isBlank()) {
      throw new IllegalStateException("GOOGLE_ROUTES_API_KEY_NOT_CONFIGURED");
    }
    if (origins.isEmpty() || destinations.isEmpty()) {
      return List.of();
    }

    String response =
        restClient
            .post()
            .uri("/distanceMatrix/v2:computeRouteMatrix")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Goog-Api-Key", properties.apiKey())
            .header("X-Goog-FieldMask", FIELD_MASK)
            .body(
                new MatrixRequest(
                    toWaypoints(origins), toWaypoints(destinations), "DRIVE", "TRAFFIC_UNAWARE"))
            .retrieve()
            .body(String.class);
    return parse(response);
  }

  private List<MatrixElement> parse(String response) {
    try {
      List<GoogleMatrixElement> elements =
          objectMapper.readValue(response, new TypeReference<List<GoogleMatrixElement>>() {});
      return elements.stream().map(this::toMatrixElement).toList();
    } catch (JsonProcessingException arrayException) {
      try {
        List<GoogleMatrixElement> elements = new ArrayList<>();
        MappingIterator<GoogleMatrixElement> iterator =
            objectMapper.readerFor(GoogleMatrixElement.class).readValues(response);
        while (iterator.hasNextValue()) {
          elements.add(iterator.nextValue());
        }
        return elements.stream().map(this::toMatrixElement).toList();
      } catch (IOException streamException) {
        throw new IllegalStateException("GOOGLE_ROUTES_INVALID_RESPONSE", streamException);
      }
    }
  }

  private MatrixElement toMatrixElement(GoogleMatrixElement element) {
    if (!"ROUTE_EXISTS".equals(element.condition())
        || element.distanceMeters() == null
        || element.duration() == null) {
      return new MatrixElement(
          element.originIndex(),
          element.destinationIndex(),
          null,
          null,
          element.status() == null || element.status().message() == null
              ? "ROUTE_NOT_FOUND"
              : element.status().message());
    }
    return new MatrixElement(
        element.originIndex(),
        element.destinationIndex(),
        element.distanceMeters(),
        durationSeconds(element.duration()),
        null);
  }

  private static int durationSeconds(String duration) {
    if (!duration.endsWith("s")) {
      throw new IllegalArgumentException("GOOGLE_ROUTES_INVALID_DURATION");
    }
    return new BigDecimal(duration.substring(0, duration.length() - 1))
        .setScale(0, RoundingMode.HALF_UP)
        .intValueExact();
  }

  private static List<RouteMatrixWaypoint> toWaypoints(List<AttractionPoint> points) {
    return points.stream()
        .map(
            point ->
                new RouteMatrixWaypoint(
                    new Waypoint(new Location(new LatLng(point.latitude(), point.longitude())))))
        .toList();
  }

  private record MatrixRequest(
      List<RouteMatrixWaypoint> origins,
      List<RouteMatrixWaypoint> destinations,
      String travelMode,
      String routingPreference) {}

  private record RouteMatrixWaypoint(Waypoint waypoint) {}

  private record Waypoint(Location location) {}

  private record Location(LatLng latLng) {}

  private record LatLng(BigDecimal latitude, BigDecimal longitude) {}

  private record GoogleMatrixElement(
      int originIndex,
      int destinationIndex,
      String condition,
      Integer distanceMeters,
      String duration,
      GoogleStatus status) {}

  private record GoogleStatus(String message) {}
}
