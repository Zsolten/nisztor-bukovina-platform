package com.bukovina.platform.accommodation.roomtype.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdminRoomTypeUpdateRequest(
    @NotBlank @Size(max = 80) @Pattern(regexp = "^[a-z0-9]+(?:_[a-z0-9]+)*$") String code,
    @Min(0) @Max(15) int quantity,
    @Min(1) @Max(4) int standardOccupancy,
    @Min(0) @Max(15) int roomsWithExtraBed,
    @Min(0) @Max(4) int extraBedsPerEligibleRoom,
    boolean active,
    @NotNull List<@Valid AdminRoomTypeTranslationUpdateRequest> translations) {}
