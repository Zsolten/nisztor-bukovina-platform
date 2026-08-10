package com.bukovina.platform.accommodation.pricing.dao;

import com.bukovina.platform.accommodation.pricing.service.PricingView;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PricingQueryDao {

  private final JdbcClient jdbcClient;

  public PricingQueryDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public PricingView findPublished(UUID guesthouseId, String language) {
    PricingHeader header =
        jdbcClient
            .sql(
                """
                SELECT pricing.id, pricing.currency,
                       COALESCE(requested.payment_note, fallback.payment_note) AS payment_note
                FROM guesthouse_pricing pricing
                LEFT JOIN guesthouse_pricing_translation requested
                  ON requested.pricing_id = pricing.id
                 AND requested.language_code = :language
                JOIN guesthouse_pricing_translation fallback
                  ON fallback.pricing_id = pricing.id
                 AND fallback.language_code = 'hu'
                WHERE pricing.guesthouse_id = :guesthouseId
                  AND pricing.active = TRUE
                """)
            .param("guesthouseId", guesthouseId)
            .param("language", language)
            .query(
                (resultSet, rowNumber) ->
                    new PricingHeader(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("currency"),
                        resultSet.getString("payment_note")))
            .optional()
            .orElseThrow(() -> new IllegalStateException("Guesthouse has no active pricing"));

    return new PricingView(
        header.currency(),
        findItems(header.id(), language),
        findTaxes(language),
        findAdjustments(header.id(), language, "SURCHARGE"),
        findAdjustments(header.id(), language, "DISCOUNT"),
        header.paymentNote());
  }

  private List<PricingView.Item> findItems(UUID pricingId, String language) {
    return jdbcClient
        .sql(
            """
            SELECT item.code, COALESCE(requested.label, fallback.label) AS label,
                   item.amount, item.unit
            FROM price_item item
            JOIN price_item_language_availability availability
              ON availability.price_item_id = item.id
             AND availability.language_code = :language
            LEFT JOIN price_item_translation requested
              ON requested.price_item_id = item.id
             AND requested.language_code = :language
            JOIN price_item_translation fallback
              ON fallback.price_item_id = item.id
             AND fallback.language_code = 'hu'
            WHERE item.pricing_id = :pricingId
              AND item.active = TRUE
            ORDER BY item.display_order
            """)
        .param("pricingId", pricingId)
        .param("language", language)
        .query(
            (resultSet, rowNumber) ->
                new PricingView.Item(
                    resultSet.getString("code"),
                    resultSet.getString("label"),
                    resultSet.getBigDecimal("amount"),
                    resultSet.getString("unit")))
        .list();
  }

  private List<PricingView.Adjustment> findAdjustments(
      UUID pricingId, String language, String kind) {
    return jdbcClient
        .sql(
            """
            SELECT adjustment.code,
                   COALESCE(requested.label, fallback.label) AS label,
                   adjustment.percentage
            FROM pricing_adjustment adjustment
            LEFT JOIN pricing_adjustment_translation requested
              ON requested.adjustment_id = adjustment.id
             AND requested.language_code = :language
            JOIN pricing_adjustment_translation fallback
              ON fallback.adjustment_id = adjustment.id
             AND fallback.language_code = 'hu'
            WHERE adjustment.pricing_id = :pricingId
              AND adjustment.kind = :kind
              AND adjustment.active = TRUE
            ORDER BY adjustment.display_order
            """)
        .param("pricingId", pricingId)
        .param("language", language)
        .param("kind", kind)
        .query(
            (resultSet, rowNumber) ->
                new PricingView.Adjustment(
                    resultSet.getString("code"),
                    resultSet.getString("label"),
                    resultSet.getBigDecimal("percentage")))
        .list();
  }

  private List<PricingView.Adjustment> findTaxes(String language) {
    return jdbcClient
        .sql(
            """
            SELECT tax.code,
                   COALESCE(requested.label, fallback.label) AS label,
                   tax.percentage
            FROM tax_configuration tax
            LEFT JOIN tax_configuration_translation requested
              ON requested.tax_code = tax.code
             AND requested.language_code = :language
            JOIN tax_configuration_translation fallback
              ON fallback.tax_code = tax.code
             AND fallback.language_code = 'hu'
            WHERE tax.active = TRUE
            ORDER BY tax.display_order
            """)
        .param("language", language)
        .query(
            (resultSet, rowNumber) ->
                new PricingView.Adjustment(
                    resultSet.getString("code"),
                    resultSet.getString("label"),
                    resultSet.getBigDecimal("percentage")))
        .list();
  }

  private record PricingHeader(UUID id, String currency, String paymentNote) {}
}
