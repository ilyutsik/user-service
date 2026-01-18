package org.innowise.userservice.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("user with id:" + id + " not found");
    }

    public UserNotFoundException(String email) {
        super("user with email:" + email + " not found");
    }

}
