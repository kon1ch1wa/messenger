package ru.shutoff.messenger.domain.file_handling.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.file_handling.dto.AbstractFile;
import ru.shutoff.messenger.domain.file_handling.model.FileEntity;
import ru.shutoff.messenger.domain.file_handling.repository.FileInfoRepository;
import ru.shutoff.messenger.domain.file_handling.repository.MinioFileStorage;

@Service
@RequiredArgsConstructor
public class FileService {
    private final @NonNull MinioFileStorage minioFileStorage;
    private final @NonNull FileInfoRepository fileInfoRepository;

    public FileEntity uploadFile(String fileName, InputStream fileBody, long fileSize) {
        UUID fileId = UUID.randomUUID();
        minioFileStorage.store(fileId.toString(), fileBody);
        FileEntity file = FileEntity
            .builder()
            .fileId(fileId)
            .filePath(fileName)
            .fileSize(fileSize)
            .build();
        fileInfoRepository.save(file);
        return file;
    }

    public AbstractFile downloadFile(UUID fileId) {
        FileEntity file = fileInfoRepository.get(fileId);
        byte[] content = minioFileStorage.get(file.getFilePath(), file.getFileSize());
        return new AbstractFile(file.getFilePath(), content);
    }
}
