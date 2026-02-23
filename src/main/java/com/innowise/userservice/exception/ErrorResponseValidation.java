package com.innowise.userservice.exception;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseValidation {

  int status;
  String error;
  private Map<String, String> errors;
}
