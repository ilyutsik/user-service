package com.innowise.userservice.constant;

public final class UserApi {

  public static final String BASE = "/api/v1/users";
  public static final String ID = "/{id}";
  public static final String ACTIVATE = "/activate/{id}";
  public static final String DEACTIVATE = "/deactivate/{id}";

  private UserApi() {
  }
}
