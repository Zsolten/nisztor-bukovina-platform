package com.bukovina.platform.accommodation.roomtype.controller;

import com.bukovina.platform.accommodation.roomtype.service.AdminRoomTypeException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AdminRoomTypeController.class)
public class AdminRoomTypeExceptionHandler {

  @ExceptionHandler(AdminRoomTypeException.class)
  ResponseEntity<AdminRoomTypeErrorResponse> roomTypeError(AdminRoomTypeException exception) {
    String[] parts = exception.getCode().split(":", 3);
    HttpStatus status =
        switch (parts[0]) {
          case "ADMIN_ROOM_TYPE_GUESTHOUSE_NOT_FOUND", "ROOM_TYPE_NOT_FOUND" ->
              HttpStatus.NOT_FOUND;
          default -> HttpStatus.BAD_REQUEST;
        };
    Map<String, String> fieldErrors = parts.length == 3 ? Map.of(parts[1], parts[2]) : Map.of();
    return ResponseEntity.status(status)
        .body(new AdminRoomTypeErrorResponse(parts[0], fieldErrors));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<AdminRoomTypeErrorResponse> invalidFields(
      MethodArgumentNotValidException exception) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      fields.putIfAbsent(error.getField(), "INVALID");
    }
    return ResponseEntity.badRequest()
        .body(new AdminRoomTypeErrorResponse("ADMIN_ROOM_TYPE_VALIDATION_FAILED", fields));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<AdminRoomTypeErrorResponse> invalidBody() {
    return ResponseEntity.badRequest()
        .body(new AdminRoomTypeErrorResponse("INVALID_ADMIN_ROOM_TYPE_REQUEST", Map.of()));
  }
}
