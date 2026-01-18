package org.innowise.userservice.exception;

public class MaxCardsExceededException extends RuntimeException {
    public MaxCardsExceededException(Long userId) {
        super("User " + userId + " already has 5 active cards");
    }
}
