package com.bukovina.platform;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@Import(PostgreSqlTestContainerConfiguration.class)
class BukovinaPlatformApplicationTests {

  @Test
  void contextLoads() {}
}
