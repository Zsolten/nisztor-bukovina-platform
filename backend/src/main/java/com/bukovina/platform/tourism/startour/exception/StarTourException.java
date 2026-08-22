package com.bukovina.platform.tourism.startour.exception;

import org.springframework.http.HttpStatus;

public class StarTourException extends RuntimeException {
  private final HttpStatus status;
  private final String code;

  public StarTourException(HttpStatus status, String code) {
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

  public static StarTourException badRequest(String code) {
    return new StarTourException(HttpStatus.BAD_REQUEST, code);
  }

  public static StarTourException notFound() {
    return new StarTourException(HttpStatus.NOT_FOUND, "STAR_TOUR_NOT_FOUND");
  }

  public static StarTourException slugExists() {
    return new StarTourException(HttpStatus.CONFLICT, "STAR_TOUR_SLUG_EXISTS");
  }

  public static StarTourException rateLimited(String code) {
    return new StarTourException(HttpStatus.TOO_MANY_REQUESTS, code);
  }
}
