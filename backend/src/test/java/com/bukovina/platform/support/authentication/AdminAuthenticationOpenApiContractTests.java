package com.bukovina.platform.support.authentication;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class AdminAuthenticationOpenApiContractTests {

  @Test
  void documentsAdministratorLoginLogoutAndBearerAuthentication() throws IOException {
    String contract = Files.readString(Path.of("..", "docs", "api", "openapi.yaml"));
    Map<?, ?> document = new Yaml().load(contract);
    Map<?, ?> paths = (Map<?, ?>) document.get("paths");

    assertTrue(paths.containsKey("/admin/auth/login"));
    assertTrue(paths.containsKey("/admin/auth/logout"));
    assertTrue(contract.contains("AdminBearerAuth"));
    assertTrue(contract.contains("bearerFormat: JWT"));
    assertTrue(contract.contains("INVALID_ADMIN_CREDENTIALS"));
  }
}
