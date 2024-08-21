package ru.shutoff.messenger.domain.file_handling.exception;

public class InvalidFileDataException extends RuntimeException {
    public InvalidFileDataException(String message) {
        super(message);
    }
}
