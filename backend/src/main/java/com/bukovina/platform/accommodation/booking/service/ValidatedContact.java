package com.bukovina.platform.accommodation.booking.service;

public record ValidatedContact(
    String name, String email, String phone, String preferredLanguage, String note) {}
