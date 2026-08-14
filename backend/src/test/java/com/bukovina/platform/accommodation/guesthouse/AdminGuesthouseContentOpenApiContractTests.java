package com.bukovina.platform.accommodation.guesthouse;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class AdminGuesthouseContentOpenApiContractTests {

  @Test
  void documentsProtectedVersionedGuesthouseContentEditing() throws IOException {
    String contract = Files.readString(Path.of("..", "docs", "api", "openapi.yaml"));
    Map<?, ?> document = new Yaml().load(contract);
    Map<?, ?> paths = (Map<?, ?>) document.get("paths");

    assertTrue(paths.containsKey("/admin/guesthouses/content"));
    assertTrue(paths.containsKey("/admin/guesthouses/{guesthouseId}/translations/{language}"));
    assertTrue(contract.contains("AdminGuesthouseTranslationUpdate"));
    assertTrue(contract.contains("ADMIN_CONTENT_VERSION_CONFLICT"));
    assertTrue(contract.contains("ADMIN_CONTENT_VALIDATION_FAILED"));
    assertTrue(contract.contains("maxLength: 5000"));
    assertTrue(contract.contains("AdminBearerAuth"));
  }
}
