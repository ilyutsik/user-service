package org.innowise.userservice.constant;

public final class ApiConstant {
    private ApiConstant() {}
    private static final String API_V1 = "/api/v1";
    private static final String USERS = "/users";
    private static final String CARDS = "/cards";
    private static final String ID_PATH = "/{id}";
    private static final String ACTIVATE_PATH = "/activate";
    private static final String DEACTIVATE_PATH = "/deactivate";

    public static final String USERS_BASE = API_V1 + USERS;
    public static final String CARDS_BASE = API_V1 + CARDS;

    public static final String USER_ID_PATH = ID_PATH;
    public static final String CARD_ID_PATH = ID_PATH;

    public static final String ACTIVATE_USER = ACTIVATE_PATH + ID_PATH;
    public static final String DEACTIVATE_USER = DEACTIVATE_PATH + ID_PATH;
    public static final String ACTIVATE_CARD = ACTIVATE_PATH + ID_PATH;
    public static final String DEACTIVATE_CARD = DEACTIVATE_PATH + ID_PATH;

    public static final String CARDS_BY_USER_ID = "/user/{userId}";
}