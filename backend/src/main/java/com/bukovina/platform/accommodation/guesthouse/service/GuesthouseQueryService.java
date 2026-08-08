package com.bukovina.platform.accommodation.guesthouse.service;

import com.bukovina.platform.accommodation.amenity.service.AmenityQuery;
import com.bukovina.platform.accommodation.amenity.service.AmenityView;
import com.bukovina.platform.accommodation.guesthouse.dao.GuesthouseContentQueryDao;
import com.bukovina.platform.accommodation.guesthouse.dao.GuesthouseRepository;
import com.bukovina.platform.accommodation.guesthouse.dto.AmenityResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseDetailResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseHistoryResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseImageResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.GuesthousePricingResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseSummaryResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.PriceItemResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.PricingAdjustmentResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.RoomTypeResponse;
import com.bukovina.platform.accommodation.guesthouse.model.Guesthouse;
import com.bukovina.platform.accommodation.guesthouse.model.GuesthouseTranslation;
import com.bukovina.platform.accommodation.pricing.service.PricingQuery;
import com.bukovina.platform.accommodation.pricing.service.PricingView;
import com.bukovina.platform.accommodation.roomtype.service.RoomTypeQuery;
import com.bukovina.platform.accommodation.roomtype.service.RoomTypeView;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuesthouseQueryService {

  private static final String DEFAULT_LANGUAGE = "hu";

  private final GuesthouseRepository guesthouseRepository;
  private final GuesthouseContentQueryDao contentQueryDao;
  private final RoomTypeQuery roomTypeQuery;
  private final AmenityQuery amenityQuery;
  private final PricingQuery pricingQuery;

  public GuesthouseQueryService(
      GuesthouseRepository guesthouseRepository,
      GuesthouseContentQueryDao contentQueryDao,
      RoomTypeQuery roomTypeQuery,
      AmenityQuery amenityQuery,
      PricingQuery pricingQuery) {
    this.guesthouseRepository = guesthouseRepository;
    this.contentQueryDao = contentQueryDao;
    this.roomTypeQuery = roomTypeQuery;
    this.amenityQuery = amenityQuery;
    this.pricingQuery = pricingQuery;
  }

  @Transactional(readOnly = true)
  public List<GuesthouseSummaryResponse> listActive(String language) {
    return guesthouseRepository.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
        .map(guesthouse -> toSummary(guesthouse, language))
        .toList();
  }

  @Transactional(readOnly = true)
  public GuesthouseDetailResponse getActive(String slug, String language) {
    Guesthouse guesthouse =
        guesthouseRepository
            .findBySlugAndActiveTrue(slug)
            .orElseThrow(() -> new GuesthouseNotFoundException(slug));
    GuesthouseTranslation translation = translationFor(guesthouse, language);
    List<GuesthouseImageResponse> images = contentQueryDao.findImages(guesthouse.getId(), language);
    PricingView pricing = pricingQuery.findPublished(guesthouse.getId(), language);

    return new GuesthouseDetailResponse(
        guesthouse.getSlug(),
        translation.getName(),
        translation.getShortDescription(),
        guesthouse.getRoomCount(),
        coverImage(images),
        translation.getDescription(),
        translation.getRoomDescription(),
        images,
        new GuesthouseHistoryResponse(translation.getHistoryTitle(), translation.getHistoryText()),
        contentQueryDao.findContacts(guesthouse.getId(), language),
        contentQueryDao.findAddress(guesthouse.getId(), language),
        roomTypeQuery.findPublished(guesthouse.getId(), language).stream()
            .map(this::toRoomType)
            .toList(),
        amenityQuery.findPublished(guesthouse.getId(), language).stream()
            .map(this::toAmenity)
            .toList(),
        toPricing(pricing));
  }

  private GuesthouseSummaryResponse toSummary(Guesthouse guesthouse, String language) {
    GuesthouseTranslation translation = translationFor(guesthouse, language);
    return new GuesthouseSummaryResponse(
        guesthouse.getSlug(),
        translation.getName(),
        translation.getShortDescription(),
        guesthouse.getRoomCount(),
        coverImage(contentQueryDao.findImages(guesthouse.getId(), language)));
  }

  private GuesthouseTranslation translationFor(Guesthouse guesthouse, String language) {
    return guesthouse.getTranslations().stream()
        .filter(translation -> translation.getLanguageCode().equals(language))
        .findFirst()
        .or(
            () ->
                guesthouse.getTranslations().stream()
                    .filter(translation -> translation.getLanguageCode().equals(DEFAULT_LANGUAGE))
                    .findFirst())
        .orElseThrow(() -> new IllegalStateException("Guesthouse has no Hungarian translation"));
  }

  private GuesthouseImageResponse coverImage(List<GuesthouseImageResponse> images) {
    return images.stream()
        .filter(GuesthouseImageResponse::cover)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Guesthouse has no cover image"));
  }

  private RoomTypeResponse toRoomType(RoomTypeView roomType) {
    return new RoomTypeResponse(
        roomType.id(),
        roomType.name(),
        roomType.quantity(),
        roomType.standardOccupancy(),
        roomType.roomsWithExtraBed(),
        roomType.extraBedsPerEligibleRoom(),
        roomType.features());
  }

  private AmenityResponse toAmenity(AmenityView amenity) {
    return new AmenityResponse(
        amenity.id(), amenity.name(), amenity.description(), amenity.category());
  }

  private GuesthousePricingResponse toPricing(PricingView pricing) {
    return new GuesthousePricingResponse(
        pricing.currency(),
        pricing.items().stream()
            .map(item -> new PriceItemResponse(item.id(), item.label(), item.amount(), item.unit()))
            .toList(),
        pricing.taxes().stream().map(this::toAdjustment).toList(),
        pricing.surcharges().stream().map(this::toAdjustment).toList(),
        pricing.discounts().stream().map(this::toAdjustment).toList(),
        pricing.paymentNote());
  }

  private PricingAdjustmentResponse toAdjustment(PricingView.Adjustment adjustment) {
    return new PricingAdjustmentResponse(
        adjustment.id(), adjustment.label(), adjustment.percentage());
  }
}
