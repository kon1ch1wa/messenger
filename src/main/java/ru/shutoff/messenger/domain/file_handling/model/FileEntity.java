package ru.shutoff.messenger.domain.file_handling.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class FileEntity {
    UUID fileId;
    String filePath;
    Long fileSize;
}
