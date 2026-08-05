package com.bukovina.platform.accommodation.guesthouse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class GuesthouseGalleryMigrationUpgradeTests {

  @Container
  private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

  @Test
  void preservesImagesAddedOutsideTheSeedData() {
    migrateToVersionTwo();

    JdbcTemplate jdbcTemplate = jdbcTemplate();
    jdbcTemplate.update(
        """
        INSERT INTO guesthouse_image (
            guesthouse_id, path, alt_text, display_order, cover
        )
        SELECT id, '/uploads/guesthouses/nisztor/custom-event.jpg',
               'Owner-added event photo', 999, FALSE
        FROM guesthouse
        WHERE slug = 'nisztor-panzio'
        """);
    jdbcTemplate.update(
        """
        INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
        SELECT id, 'en', 'Owner-added event photo'
        FROM guesthouse_image
        WHERE path = '/uploads/guesthouses/nisztor/custom-event.jpg'
        """);

    migrateToLatestVersion();

    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM guesthouse_image WHERE path = ?",
            Integer.class,
            "/uploads/guesthouses/nisztor/custom-event.jpg"));
    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM guesthouse_image_translation translation
            JOIN guesthouse_image image ON image.id = translation.image_id
            WHERE image.path = '/uploads/guesthouses/nisztor/custom-event.jpg'
            """,
            Integer.class));
  }

  private void migrateToVersionTwo() {
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .target(MigrationVersion.fromVersion("2"))
        .load()
        .migrate();
  }

  private void migrateToLatestVersion() {
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .load()
        .migrate();
  }

  private JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(
        new DriverManagerDataSource(
            POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()));
  }
}
