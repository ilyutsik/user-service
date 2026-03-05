package com.innowise.userservice.exception;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User Not Found",
        ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExist(UserAlreadyExistsException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(),
        "User Already Exist", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(PaymentCardNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCardInfoNotFound(PaymentCardNotFoundException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
        "Payment Card Not Found", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MaxCardsExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxCardsExceeded(MaxCardsExceededException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
        "Max Cards Exceeded", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(),
        "Forbidden", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(errorResponse);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseValidation> handleValidationException(
      MethodArgumentNotValidException ex) {

    Map<String, String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
            (msg1, msg2) -> msg1 + "; " + msg2));

    ErrorResponseValidation response = new ErrorResponseValidation(HttpStatus.BAD_REQUEST.value(),
        "Validation Failed", errors);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal ServerError", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
