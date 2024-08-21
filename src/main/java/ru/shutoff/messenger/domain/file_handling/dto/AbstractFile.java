package ru.shutoff.messenger.domain.file_handling.dto;

public record AbstractFile(
    String fileName,
    byte[] content
) {
}
