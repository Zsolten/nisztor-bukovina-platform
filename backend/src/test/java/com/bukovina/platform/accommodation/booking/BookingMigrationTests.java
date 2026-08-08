package com.bukovina.platform.accommodation.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bukovina.platform.testsupport.PostgreSqlTestContainerConfiguration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "DB_PASSWORD=test-password")
@Import(PostgreSqlTestContainerConfiguration.class)
class BookingMigrationTests {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void createsBookingTablesAndRequiredForeignKeys() {
    assertTrue(tableExists("booking_request"));
    assertTrue(tableExists("booking_room_selection"));
    assertTrue(tableExists("booking_status_history"));

    assertTrue(foreignKeyExists("booking_request", "guesthouse"));
    assertTrue(foreignKeyExists("booking_room_selection", "booking_request"));
    assertTrue(foreignKeyExists("booking_room_selection", "room_type"));
    assertTrue(foreignKeyExists("booking_status_history", "booking_request"));
  }

  @Test
  void createsAdminFilteringIndexes() {
    List<String> indexDefinitions =
        jdbcTemplate.queryForList(
            "SELECT indexdef FROM pg_indexes WHERE tablename = 'booking_request'", String.class);

    assertTrue(indexDefinitions.stream().anyMatch(index -> index.contains("(guesthouse_id)")));
    assertTrue(indexDefinitions.stream().anyMatch(index -> index.contains("(status)")));
    assertTrue(indexDefinitions.stream().anyMatch(index -> index.contains("(contact_email)")));
    assertTrue(indexDefinitions.stream().anyMatch(index -> index.contains("(created_at)")));
  }

  @Test
  void restrictsStatusToBook009ValuesWithoutEmailVerificationState() {
    String constraints =
        constraintsFor("booking_request") + constraintsFor("booking_status_history");

    for (String status :
        List.of("RECEIVED", "UNDER_REVIEW", "CONFIRMED", "REJECTED", "CANCELLED")) {
      assertTrue(constraints.contains(status));
    }
    assertFalse(constraints.contains("PENDING_EMAIL_VERIFICATION"));
  }

  @Test
  void doesNotCreatePaymentCardColumns() {
    Integer paymentCardColumns =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name LIKE 'booking%'
              AND (column_name LIKE '%card%' OR column_name LIKE '%cvv%')
            """,
            Integer.class);

    assertEquals(0, paymentCardColumns);
  }

  @Test
  void storesGenericTaxRatesAndAmountsWithoutEncodingCurrentRatesInColumnNames() {
    for (String columnName :
        List.of(
            "accommodation_tax_rate",
            "accommodation_tax_amount",
            "city_tax_rate",
            "city_tax_amount")) {
      assertTrue(columnExists("booking_request", columnName));
    }

    assertFalse(columnExists("booking_request", "tax_11_percent"));
    assertFalse(columnExists("booking_request", "city_tax_1_percent"));
  }

  @Test
  void storesCountryWideTaxRatesAsEditableGlobalConfiguration() {
    assertTrue(tableExists("tax_configuration"));
    assertTrue(tableExists("tax_configuration_translation"));
    assertEquals("11.00", taxRate("accommodation_tax"));
    assertEquals("1.00", taxRate("city_tax"));
    assertTrue(foreignKeyExists("tax_configuration_translation", "tax_configuration"));

    Integer obsoleteTouristTaxRows =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM pricing_adjustment WHERE code = 'tourist_tax'", Integer.class);
    assertEquals(0, obsoleteTouristTaxRows);
  }

  private boolean tableExists(String tableName) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = 'public' AND table_name = ?
            """,
            Integer.class,
            tableName);
    return count == 1;
  }

  private String taxRate(String taxCode) {
    return jdbcTemplate.queryForObject(
        """
        SELECT percentage::TEXT
        FROM tax_configuration
        WHERE code = ?
          AND active = TRUE
        """,
        String.class,
        taxCode);
  }

  private boolean foreignKeyExists(String tableName, String referencedTableName) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM pg_constraint constraint_record
            WHERE constraint_record.contype = 'f'
              AND constraint_record.conrelid = CAST(? AS regclass)
              AND constraint_record.confrelid = CAST(? AS regclass)
            """,
            Integer.class,
            tableName,
            referencedTableName);
    return count > 0;
  }

  private boolean columnExists(String tableName, String columnName) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = ? AND column_name = ?
            """,
            Integer.class,
            tableName,
            columnName);
    return count == 1;
  }

  private String constraintsFor(String tableName) {
    return jdbcTemplate.queryForObject(
        """
        SELECT STRING_AGG(PG_GET_CONSTRAINTDEF(constraint_record.oid), ' ')
        FROM pg_constraint constraint_record
        WHERE constraint_record.conrelid = CAST(? AS regclass)
        """,
        String.class,
        tableName);
  }
}
