package ru.shutoff.messenger.domain.file_handling.exception;

public class EmptyFileUploadingException extends RuntimeException {
    public EmptyFileUploadingException(String message) {
        super(message);
    }
}
