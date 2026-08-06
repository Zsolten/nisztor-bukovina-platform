package com.bukovina.platform.accommodation.guesthouse.dto;

public record GuesthouseContactResponse(
    String type, String value, String label, boolean preferred) {}
