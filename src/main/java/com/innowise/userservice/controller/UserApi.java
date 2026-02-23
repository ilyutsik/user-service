package com.innowise.userservice.controller;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class UserApi {

  public static final String BASE = "/api/v1/users";
  public static final String ID = "/{id}";
  public static final String CARDS = "/{id}/cards";
}
