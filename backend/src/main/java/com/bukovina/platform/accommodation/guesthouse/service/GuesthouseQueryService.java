package com.bukovina.platform.accommodation.guesthouse.service;

import com.bukovina.platform.accommodation.guesthouse.dao.GuesthouseRepository;
import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseDetailResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseImageResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseSummaryResponse;
import com.bukovina.platform.accommodation.guesthouse.model.Guesthouse;
import com.bukovina.platform.accommodation.guesthouse.model.GuesthouseImage;
import com.bukovina.platform.accommodation.guesthouse.model.GuesthouseTranslation;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuesthouseQueryService {

  private static final String DEFAULT_LANGUAGE = "hu";

  private final GuesthouseRepository guesthouseRepository;

  public GuesthouseQueryService(GuesthouseRepository guesthouseRepository) {
    this.guesthouseRepository = guesthouseRepository;
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
    List<GuesthouseImageResponse> images =
        guesthouse.getImages().stream().map(this::toImage).toList();

    return new GuesthouseDetailResponse(
        guesthouse.getSlug(),
        translation.getName(),
        translation.getShortDescription(),
        guesthouse.getRoomCount(),
        coverImage(guesthouse),
        translation.getDescription(),
        translation.getRoomDescription(),
        images);
  }

  private GuesthouseSummaryResponse toSummary(Guesthouse guesthouse, String language) {
    GuesthouseTranslation translation = translationFor(guesthouse, language);
    return new GuesthouseSummaryResponse(
        guesthouse.getSlug(),
        translation.getName(),
        translation.getShortDescription(),
        guesthouse.getRoomCount(),
        coverImage(guesthouse));
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

  private GuesthouseImageResponse coverImage(Guesthouse guesthouse) {
    return guesthouse.getImages().stream()
        .filter(GuesthouseImage::isCover)
        .findFirst()
        .map(this::toImage)
        .orElseThrow(() -> new IllegalStateException("Guesthouse has no cover image"));
  }

  private GuesthouseImageResponse toImage(GuesthouseImage image) {
    return new GuesthouseImageResponse(image.getPath(), image.getAltText(), image.isCover());
  }
}
