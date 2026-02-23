package com.innowise.userservice.exception;

public class PaymentCardNotFoundException extends RuntimeException {

  public PaymentCardNotFoundException(Long id) {
    super("card info with id:" + id + " not found");
  }
}
