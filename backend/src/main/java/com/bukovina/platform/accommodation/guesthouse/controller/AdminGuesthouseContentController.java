package com.bukovina.platform.accommodation.guesthouse.controller;

import com.bukovina.platform.accommodation.guesthouse.dto.AdminGuesthouseContentResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.AdminGuesthouseTranslationResponse;
import com.bukovina.platform.accommodation.guesthouse.dto.AdminGuesthouseTranslationUpdateRequest;
import com.bukovina.platform.accommodation.guesthouse.service.AdminGuesthouseContentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/guesthouses")
public class AdminGuesthouseContentController {

  private final AdminGuesthouseContentService contentService;

  public AdminGuesthouseContentController(AdminGuesthouseContentService contentService) {
    this.contentService = contentService;
  }

  @GetMapping("/content")
  public List<AdminGuesthouseContentResponse> list() {
    return contentService.list();
  }

  @PutMapping("/{guesthouseId}/translations/{language}")
  public AdminGuesthouseTranslationResponse update(
      @PathVariable UUID guesthouseId,
      @PathVariable String language,
      @Valid @RequestBody AdminGuesthouseTranslationUpdateRequest request) {
    return contentService.update(guesthouseId, language, request);
  }
}
