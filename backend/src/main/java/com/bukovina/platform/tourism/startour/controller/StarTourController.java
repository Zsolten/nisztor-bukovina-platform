package com.bukovina.platform.tourism.startour.controller;

import com.bukovina.platform.tourism.startour.service.StarTourRouteService;
import com.bukovina.platform.tourism.startour.service.StarTourRouteService.StarTourRouteResponse;
import com.bukovina.platform.tourism.startour.service.StarTourService;
import com.bukovina.platform.tourism.startour.service.StarTourService.StarTourPublicResponse;
import com.bukovina.platform.tourism.startour.service.StarTourService.StarTourResponse;
import com.bukovina.platform.tourism.startour.service.StarTourService.StarTourUpsertRequest;
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
public class StarTourController {
  private final StarTourService service;
  private final StarTourRouteService routeService;

  public StarTourController(StarTourService service, StarTourRouteService routeService) {
    this.service = service;
    this.routeService = routeService;
  }

  @GetMapping("/api/admin/tourism/star-tours")
  List<StarTourResponse> listAdmin() {
    return service.listAdmin();
  }

  @PostMapping("/api/admin/tourism/star-tours")
  @ResponseStatus(HttpStatus.CREATED)
  StarTourResponse create(@RequestBody StarTourUpsertRequest request) {
    return service.create(request);
  }

  @PutMapping("/api/admin/tourism/star-tours/{id}")
  StarTourResponse update(@PathVariable UUID id, @RequestBody StarTourUpsertRequest request) {
    return service.update(id, request);
  }

  @GetMapping("/api/tourism/star-tours")
  List<StarTourPublicResponse> listPublic(
      @RequestParam(defaultValue = "hu") @Pattern(regexp = "hu|ro|en") String lang) {
    return service.listPublic(lang);
  }

  @GetMapping("/api/tourism/star-tours/{slug}/route")
  StarTourRouteResponse getPublicRoute(
      @PathVariable String slug,
      @RequestParam(name = "optionalStopSlug", required = false) List<String> optionalStopSlugs) {
    return routeService.getPublicRoute(slug, optionalStopSlugs);
  }

  @GetMapping("/api/tourism/star-tours/{slug}")
  StarTourPublicResponse getPublic(
      @PathVariable String slug,
      @RequestParam(defaultValue = "hu") @Pattern(regexp = "hu|ro|en") String lang) {
    return service.getPublic(slug, lang);
  }
}
