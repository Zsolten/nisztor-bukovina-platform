package com.bukovina.platform.accommodation.guesthouse.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GuesthouseNotFoundException extends RuntimeException {

  public GuesthouseNotFoundException(String slug) {
    super("Active guesthouse not found: " + slug);
  }
}
