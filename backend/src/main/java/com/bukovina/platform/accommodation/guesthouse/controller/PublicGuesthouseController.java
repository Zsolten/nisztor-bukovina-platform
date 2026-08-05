package com.bukovina.platform.accommodation.guesthouse.controller;

import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseDetailResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.GuesthouseSummaryResponse;
import com.bukovina.platform.accommodation.guesthouse.service.GuesthouseQueryService;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guesthouses")
public class PublicGuesthouseController {

  private static final String LANGUAGE_PATTERN = "hu|ro|en";

  private final GuesthouseQueryService guesthouseQueryService;

  public PublicGuesthouseController(GuesthouseQueryService guesthouseQueryService) {
    this.guesthouseQueryService = guesthouseQueryService;
  }

  @GetMapping
  public List<GuesthouseSummaryResponse> list(
      @RequestParam(defaultValue = "hu") @Pattern(regexp = LANGUAGE_PATTERN) String lang) {
    return guesthouseQueryService.listActive(lang);
  }

  @GetMapping("/{slug}")
  public GuesthouseDetailResponse get(
      @PathVariable @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") String slug,
      @RequestParam(defaultValue = "hu") @Pattern(regexp = LANGUAGE_PATTERN) String lang) {
    return guesthouseQueryService.getActive(slug, lang);
  }
}
