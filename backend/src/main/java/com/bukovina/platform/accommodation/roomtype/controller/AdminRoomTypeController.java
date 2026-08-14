package com.bukovina.platform.accommodation.roomtype.controller;

import com.bukovina.platform.accommodation.roomtype.dto.AdminRoomTypeOrderUpdateRequest;
import com.bukovina.platform.accommodation.roomtype.dto.AdminRoomTypeResponse;
import com.bukovina.platform.accommodation.roomtype.dto.AdminRoomTypeUpdateRequest;
import com.bukovina.platform.accommodation.roomtype.service.AdminRoomTypeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/guesthouses/{guesthouseId}/room-types")
public class AdminRoomTypeController {

  private final AdminRoomTypeService roomTypeService;

  public AdminRoomTypeController(AdminRoomTypeService roomTypeService) {
    this.roomTypeService = roomTypeService;
  }

  @GetMapping
  public List<AdminRoomTypeResponse> list(@PathVariable UUID guesthouseId) {
    return roomTypeService.list(guesthouseId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AdminRoomTypeResponse create(
      @PathVariable UUID guesthouseId, @Valid @RequestBody AdminRoomTypeUpdateRequest request) {
    return roomTypeService.create(guesthouseId, request);
  }

  @PutMapping("/{roomTypeId}")
  public AdminRoomTypeResponse update(
      @PathVariable UUID guesthouseId,
      @PathVariable UUID roomTypeId,
      @Valid @RequestBody AdminRoomTypeUpdateRequest request) {
    return roomTypeService.update(guesthouseId, roomTypeId, request);
  }

  @PutMapping("/order")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reorder(
      @PathVariable UUID guesthouseId,
      @Valid @RequestBody AdminRoomTypeOrderUpdateRequest request) {
    roomTypeService.reorder(guesthouseId, request);
  }
}
