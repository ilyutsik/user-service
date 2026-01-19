package com.innowise.userservice.constant;

public final class CardApi {

  public static final String BASE = "/api/v1/cards";
  public static final String ID = "/{id}";
  public static final String ACTIVATE = "/activate/{id}";
  public static final String DEACTIVATE = "/deactivate/{id}";
  public static final String BY_USER_ID = "/user/{userId}";

  private CardApi() {
  }
}