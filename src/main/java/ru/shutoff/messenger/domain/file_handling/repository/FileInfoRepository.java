package ru.shutoff.messenger.domain.file_handling.repository;

import java.util.UUID;

import ru.shutoff.messenger.domain.file_handling.model.FileEntity;

public interface FileInfoRepository {
    void save(FileEntity file);
    FileEntity get(UUID fileId);
}
