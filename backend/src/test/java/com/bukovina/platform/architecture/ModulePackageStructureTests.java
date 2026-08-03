package com.bukovina.platform.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ModulePackageStructureTests {

  private static final Path SOURCE_ROOT = Path.of("src", "main", "java");

  @ParameterizedTest(name = "{0} is documented")
  @MethodSource("modulePackages")
  void packageHasDocumentation(String packageName) {
    Path packageInfo =
        SOURCE_ROOT.resolve(packageName.replace('.', '/')).resolve("package-info.java");

    assertTrue(Files.isRegularFile(packageInfo), () -> "Missing " + packageInfo);
  }

  static Stream<String> modulePackages() {
    return Stream.of(
        "com.bukovina.platform.accommodation",
        "com.bukovina.platform.accommodation.guesthouse",
        "com.bukovina.platform.accommodation.roomtype",
        "com.bukovina.platform.accommodation.pricing",
        "com.bukovina.platform.accommodation.amenity",
        "com.bukovina.platform.accommodation.booking",
        "com.bukovina.platform.tourism",
        "com.bukovina.platform.tourism.activity",
        "com.bukovina.platform.tourism.startour",
        "com.bukovina.platform.tourism.itinerary",
        "com.bukovina.platform.support",
        "com.bukovina.platform.support.authentication",
        "com.bukovina.platform.support.administration",
        "com.bukovina.platform.support.translation",
        "com.bukovina.platform.support.notification",
        "com.bukovina.platform.shared",
        "com.bukovina.platform.shared.configuration",
        "com.bukovina.platform.shared.exception",
        "com.bukovina.platform.shared.validation");
  }
}
