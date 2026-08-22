package com.bukovina.platform.tourism.activity.exception;

import org.springframework.http.HttpStatus;

public class AttractionException extends RuntimeException {
  private final HttpStatus status;
  private final String code;

  public AttractionException(HttpStatus status, String code) {
    super(code);
    this.status = status;
    this.code = code;
  }

  public HttpStatus status() {
    return status;
  }

  public String code() {
    return code;
  }

  public static AttractionException badRequest(String code) {
    return new AttractionException(HttpStatus.BAD_REQUEST, code);
  }

  public static AttractionException notFound() {
    return new AttractionException(HttpStatus.NOT_FOUND, "ATTRACTION_NOT_FOUND");
  }

  public static AttractionException slugExists() {
    return new AttractionException(HttpStatus.CONFLICT, "ATTRACTION_SLUG_EXISTS");
  }
}
