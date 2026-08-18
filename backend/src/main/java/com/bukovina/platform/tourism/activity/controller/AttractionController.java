package com.bukovina.platform.tourism.activity.controller;

import com.bukovina.platform.tourism.activity.service.AttractionService;
import com.bukovina.platform.tourism.activity.service.AttractionService.AttractionPublicResponse;
import com.bukovina.platform.tourism.activity.service.AttractionService.AttractionResponse;
import com.bukovina.platform.tourism.activity.service.AttractionService.AttractionUpsertRequest;
import com.bukovina.platform.tourism.activity.service.AttractionService.CollectionResponse;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AttractionController {
  private final AttractionService service;

  public AttractionController(AttractionService service) {
    this.service = service;
  }

  @GetMapping("/api/admin/tourism/attractions")
  List<AttractionResponse> listAdmin() {
    return service.listAdmin();
  }

  @PostMapping("/api/admin/tourism/attractions")
  @ResponseStatus(HttpStatus.CREATED)
  AttractionResponse create(@RequestBody AttractionUpsertRequest request) {
    return service.create(request);
  }

  @PutMapping("/api/admin/tourism/attractions/{id}")
  AttractionResponse update(@PathVariable UUID id, @RequestBody AttractionUpsertRequest request) {
    return service.update(id, request);
  }

  @GetMapping("/api/tourism/attractions")
  List<AttractionPublicResponse> listPublic(
      @RequestParam(defaultValue = "hu") @Pattern(regexp = "hu|ro|en") String lang) {
    return service.listPublic(lang);
  }

  @GetMapping("/api/tourism/collections")
  List<CollectionResponse> listCollections(
      @RequestParam(defaultValue = "hu") @Pattern(regexp = "hu|ro|en") String lang) {
    return service.listCollections(lang);
  }

  @GetMapping("/api/tourism/attractions/{slug}")
  AttractionPublicResponse getPublic(
      @PathVariable String slug,
      @RequestParam(defaultValue = "hu") @Pattern(regexp = "hu|ro|en") String lang) {
    return service.getPublic(slug, lang);
  }
}
