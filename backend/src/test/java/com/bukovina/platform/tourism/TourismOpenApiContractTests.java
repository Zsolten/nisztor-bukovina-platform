package com.bukovina.platform.tourism;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class TourismOpenApiContractTests {
  @Test
  void documentsPublicAndProtectedTourismContracts() throws IOException {
    String contract = Files.readString(Path.of("..", "docs", "api", "openapi.yaml"));
    Map<?, ?> document = new Yaml().load(contract);
    Map<?, ?> paths = (Map<?, ?>) document.get("paths");
    assertTrue(paths.containsKey("/tourism/attractions"));
    assertTrue(paths.containsKey("/tourism/star-tours"));
    assertTrue(paths.containsKey("/admin/tourism/attractions"));
    assertTrue(paths.containsKey("/admin/tourism/star-tours"));
    assertTrue(contract.contains("requested translation"));
    assertTrue(contract.contains("published: { type: boolean }"));
    assertTrue(contract.contains("googleMapsUrl:"));
    assertTrue(contract.contains("DrivingDistanceCalculation:"));
    assertTrue(contract.contains("StarTourStop:"));
    assertTrue(contract.contains("stops:"));
  }
}
