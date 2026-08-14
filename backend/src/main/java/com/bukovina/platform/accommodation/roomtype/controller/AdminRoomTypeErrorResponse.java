package com.bukovina.platform.accommodation.roomtype.controller;

import java.util.Map;

public record AdminRoomTypeErrorResponse(String code, Map<String, String> fieldErrors) {}
