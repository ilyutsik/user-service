package org.innowise.userservice.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public static UserAlreadyExistsException withEmail(String email) {
        return new UserAlreadyExistsException("Email " + email + " already in use");
    }
}
