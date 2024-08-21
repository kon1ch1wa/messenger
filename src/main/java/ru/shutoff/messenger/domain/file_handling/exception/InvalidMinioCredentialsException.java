package ru.shutoff.messenger.domain.file_handling.exception;

public class InvalidMinioCredentialsException extends RuntimeException {
    public InvalidMinioCredentialsException(String message) {
        super(message);
    }
}
