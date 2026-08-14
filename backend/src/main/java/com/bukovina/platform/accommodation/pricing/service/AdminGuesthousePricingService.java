package com.bukovina.platform.accommodation.pricing.service;

import com.bukovina.platform.accommodation.guesthouse.service.GuesthouseExistenceQuery;
import com.bukovina.platform.accommodation.pricing.dto.AdminGuesthousePricingResponse;
import com.bukovina.platform.accommodation.pricing.dto.AdminGuesthousePricingUpdateRequest;
import com.bukovina.platform.accommodation.pricing.dto.AdminPriceItemResponse;
import com.bukovina.platform.accommodation.pricing.dto.AdminPriceItemUpdateRequest;
import com.bukovina.platform.accommodation.pricing.dto.AdminPricingAdjustmentResponse;
import com.bukovina.platform.accommodation.pricing.dto.AdminPricingAdjustmentUpdateRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminGuesthousePricingService {

  private final JdbcClient jdbcClient;
  private final GuesthouseExistenceQuery guesthouseExistenceQuery;

  public AdminGuesthousePricingService(
      JdbcClient jdbcClient, GuesthouseExistenceQuery guesthouseExistenceQuery) {
    this.jdbcClient = jdbcClient;
    this.guesthouseExistenceQuery = guesthouseExistenceQuery;
  }

  @Transactional(readOnly = true)
  public AdminGuesthousePricingResponse find(UUID guesthouseId) {
    ensureGuesthouseExists(guesthouseId);
    PricingHeader header = findHeader(guesthouseId);
    return response(guesthouseId, header);
  }

  @Transactional
  public AdminGuesthousePricingResponse update(
      UUID guesthouseId, AdminGuesthousePricingUpdateRequest request) {
    ensureGuesthouseExists(guesthouseId);
    PricingHeader header = findHeader(guesthouseId);
    validateRequest(header.id(), request);

    for (AdminPriceItemUpdateRequest item : request.items()) {
      jdbcClient
          .sql(
              "UPDATE price_item SET amount = :amount WHERE pricing_id = :pricingId AND code = :code")
          .param("pricingId", header.id())
          .param("code", item.code().trim())
          .param("amount", item.amount())
          .update();
    }
    updateAdjustments(header.id(), "SURCHARGE", request.surcharges());
    updateAdjustments(header.id(), "DISCOUNT", request.discounts());
    return response(guesthouseId, header);
  }

  private void updateAdjustments(
      UUID pricingId, String kind, List<AdminPricingAdjustmentUpdateRequest> adjustments) {
    for (AdminPricingAdjustmentUpdateRequest adjustment : adjustments) {
      jdbcClient
          .sql(
              "UPDATE pricing_adjustment SET percentage = :percentage "
                  + "WHERE pricing_id = :pricingId AND kind = :kind AND code = :code")
          .param("pricingId", pricingId)
          .param("kind", kind)
          .param("code", adjustment.code().trim())
          .param("percentage", adjustment.percentage())
          .update();
    }
  }

  private AdminGuesthousePricingResponse response(UUID guesthouseId, PricingHeader header) {
    return new AdminGuesthousePricingResponse(
        guesthouseId,
        header.currency(),
        findItems(header.id()),
        findAdjustments(header.id(), "SURCHARGE"),
        findAdjustments(header.id(), "DISCOUNT"));
  }

  private PricingHeader findHeader(UUID guesthouseId) {
    return jdbcClient
        .sql(
            "SELECT id, currency FROM guesthouse_pricing "
                + "WHERE guesthouse_id = :guesthouseId AND active = TRUE")
        .param("guesthouseId", guesthouseId)
        .query(
            (resultSet, rowNumber) ->
                new PricingHeader(
                    resultSet.getObject("id", UUID.class), resultSet.getString("currency")))
        .optional()
        .orElseThrow(() -> new AdminPricingException("ADMIN_PRICING_NOT_FOUND"));
  }

  private List<AdminPriceItemResponse> findItems(UUID pricingId) {
    return jdbcClient
        .sql(
            """
            SELECT item.code, translation.label, item.amount, item.unit
            FROM price_item item
            JOIN price_item_translation translation
              ON translation.price_item_id = item.id
             AND translation.language_code = 'hu'
            WHERE item.pricing_id = :pricingId AND item.active = TRUE
            ORDER BY item.display_order
            """)
        .param("pricingId", pricingId)
        .query(
            (resultSet, rowNumber) ->
                new AdminPriceItemResponse(
                    resultSet.getString("code"),
                    resultSet.getString("label"),
                    resultSet.getBigDecimal("amount"),
                    resultSet.getString("unit")))
        .list();
  }

  private List<AdminPricingAdjustmentResponse> findAdjustments(UUID pricingId, String kind) {
    return jdbcClient
        .sql(
            """
            SELECT adjustment.code, translation.label, adjustment.percentage
            FROM pricing_adjustment adjustment
            JOIN pricing_adjustment_translation translation
              ON translation.adjustment_id = adjustment.id
             AND translation.language_code = 'hu'
            WHERE adjustment.pricing_id = :pricingId
              AND adjustment.kind = :kind
              AND adjustment.active = TRUE
            ORDER BY adjustment.display_order
            """)
        .param("pricingId", pricingId)
        .param("kind", kind)
        .query(
            (resultSet, rowNumber) ->
                new AdminPricingAdjustmentResponse(
                    resultSet.getString("code"),
                    resultSet.getString("label"),
                    resultSet.getBigDecimal("percentage")))
        .list();
  }

  private void validateRequest(UUID pricingId, AdminGuesthousePricingUpdateRequest request) {
    validateCodes(
        request.items(),
        findItems(pricingId).stream().map(AdminPriceItemResponse::code).collect(Collectors.toSet()),
        AdminPriceItemUpdateRequest::code,
        "items");
    validateCodes(
        request.surcharges(),
        findAdjustments(pricingId, "SURCHARGE").stream()
            .map(AdminPricingAdjustmentResponse::code)
            .collect(Collectors.toSet()),
        AdminPricingAdjustmentUpdateRequest::code,
        "surcharges");
    validateCodes(
        request.discounts(),
        findAdjustments(pricingId, "DISCOUNT").stream()
            .map(AdminPricingAdjustmentResponse::code)
            .collect(Collectors.toSet()),
        AdminPricingAdjustmentUpdateRequest::code,
        "discounts");
  }

  private <T> void validateCodes(
      List<T> supplied, Set<String> expected, Function<T, String> code, String field) {
    Map<String, T> byCode =
        supplied.stream()
            .collect(
                Collectors.toMap(
                    item -> code.apply(item).trim(), Function.identity(), (a, b) -> a));
    if (byCode.size() != supplied.size() || !byCode.keySet().equals(expected)) {
      throw new AdminPricingException("ADMIN_PRICING_VALIDATION_FAILED:" + field + ":INVALID_SET");
    }
  }

  private void ensureGuesthouseExists(UUID guesthouseId) {
    if (!guesthouseExistenceQuery.exists(guesthouseId)) {
      throw new AdminPricingException("ADMIN_PRICING_GUESTHOUSE_NOT_FOUND");
    }
  }

  private record PricingHeader(UUID id, String currency) {}
}
