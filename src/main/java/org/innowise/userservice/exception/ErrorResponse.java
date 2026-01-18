package org.innowise.userservice.exception;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private int status;

    private String error;

    private String massage;

}

