package com.bukovina.platform.accommodation.guesthouse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@Import(PostgreSqlTestContainerConfiguration.class)
class GuesthouseContentMigrationTests {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void seedsNormalizedLocalizedGuesthouseContent() {
    assertEquals(6, count("guesthouse_translation"));
    assertEquals(78, count("guesthouse_image_translation"));
    assertEquals(8, count("room_type"));
    assertEquals(23, count("amenity"));
    assertEquals(46, count("guesthouse_amenity"));
    assertEquals(18, count("price_item"));
    assertEquals(0, activePriceItemCount("lunch"));
    assertEquals(0, activePriceItemCount("full_board"));
    assertEquals(2, priceItemLanguageCount("tour_guide", "hu"));
    assertEquals(0, priceItemLanguageCount("tour_guide", "ro"));
    assertEquals(0, priceItemLanguageCount("tour_guide", "en"));
    assertEquals(4, count("pricing_adjustment"));
    assertEquals(12, count("guesthouse_contact"));
    assertEquals(2, count("guesthouse_address"));
  }

  @Test
  void excludesAmenitiesThatStillNeedOwnerConfirmation() {
    Integer excludedCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM amenity WHERE code IN ('bicycle_rental', 'domestic_animals')",
            Integer.class);

    assertEquals(0, excludedCount);
  }

  @Test
  void seedsRoomInventoriesForBothGuesthouses() {
    assertEquals(15, roomTypeQuantity("nisztor-panzio", "single"));
    assertEquals(15, roomTypeQuantity("nisztor-panzio", "double"));
    assertEquals(15, roomTypeQuantity("nisztor-panzio", "triple"));
    assertEquals(15, roomTypeQuantity("nisztor-panzio", "quadruple"));
    assertEquals(15, roomTypeQuantity("bukovina-panzio", "single"));
    assertEquals(15, roomTypeQuantity("bukovina-panzio", "double"));
    assertEquals(15, roomTypeQuantity("bukovina-panzio", "triple"));
    assertEquals(15, roomTypeQuantity("bukovina-panzio", "quadruple"));
  }

  @Test
  void appliesOwnerConfirmedBukovinaRoomCapacityAndCopy() {
    assertEquals(20, guesthouseRoomCount("bukovina-panzio"));
    assertEquals(
        "20 szoba egy-, két-, három- és négyágyas elrendezésben, az igényekhez igazodva.",
        roomDescription("bukovina-panzio", "hu"));
  }

  @Test
  void keepsOnlyGalleryImagesThatExistInTheFrontend() {
    assertEquals(10, galleryImageCount("nisztor-panzio"));
    assertEquals(16, galleryImageCount("bukovina-panzio"));
  }

  @Test
  void seedsLocalizedAlternativeTextForEveryGalleryImage() {
    String englishCoverAlt =
        jdbcTemplate.queryForObject(
            """
            SELECT translation.alt_text
            FROM guesthouse_image_translation translation
            JOIN guesthouse_image image ON image.id = translation.image_id
            JOIN guesthouse ON guesthouse.id = image.guesthouse_id
            WHERE guesthouse.slug = 'nisztor-panzio'
              AND image.display_order = 0
              AND translation.language_code = 'en'
            """,
            String.class);

    assertEquals("Street-facing facade of Nisztor Guesthouse", englishCoverAlt);
    assertEquals(10, distinctLocalizedAltTextCount("nisztor-panzio", "en"));
    assertEquals(16, distinctLocalizedAltTextCount("bukovina-panzio", "en"));
  }

  private int count(String tableName) {
    return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
  }

  private int activePriceItemCount(String code) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM price_item WHERE code = ? AND active = TRUE", Integer.class, code);
  }

  private int priceItemLanguageCount(String code, String languageCode) {
    return jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM price_item_language_availability availability
        JOIN price_item item ON item.id = availability.price_item_id
        WHERE item.code = ? AND availability.language_code = ?
        """,
        Integer.class,
        code,
        languageCode);
  }

  private int roomTypeQuantity(String slug, String code) {
    return jdbcTemplate.queryForObject(
        """
        SELECT room_type.quantity
        FROM room_type
        JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
        WHERE guesthouse.slug = ? AND room_type.code = ?
        """,
        Integer.class,
        slug,
        code);
  }

  private int guesthouseRoomCount(String slug) {
    return jdbcTemplate.queryForObject(
        "SELECT room_count FROM guesthouse WHERE slug = ?", Integer.class, slug);
  }

  private String roomDescription(String slug, String languageCode) {
    return jdbcTemplate.queryForObject(
        """
        SELECT translation.room_description
        FROM guesthouse_translation translation
        JOIN guesthouse ON guesthouse.id = translation.guesthouse_id
        WHERE guesthouse.slug = ? AND translation.language_code = ?
        """,
        String.class,
        slug,
        languageCode);
  }

  private int galleryImageCount(String slug) {
    return jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM guesthouse_image image
        JOIN guesthouse ON guesthouse.id = image.guesthouse_id
        WHERE guesthouse.slug = ?
        """,
        Integer.class,
        slug);
  }

  private int distinctLocalizedAltTextCount(String slug, String languageCode) {
    return jdbcTemplate.queryForObject(
        """
        SELECT COUNT(DISTINCT translation.alt_text)
        FROM guesthouse_image_translation translation
        JOIN guesthouse_image image ON image.id = translation.image_id
        JOIN guesthouse ON guesthouse.id = image.guesthouse_id
        WHERE guesthouse.slug = ? AND translation.language_code = ?
        """,
        Integer.class,
        slug,
        languageCode);
  }
}
