package ru.shutoff.messenger.domain.file_handling.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.file_handling.dto.AbstractFile;
import ru.shutoff.messenger.domain.file_handling.dto.FileResponse;
import ru.shutoff.messenger.domain.file_handling.exception.EmptyFileUploadingException;
import ru.shutoff.messenger.domain.file_handling.model.FileEntity;
import ru.shutoff.messenger.domain.file_handling.service.FileService;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FilesUploadController {
    private final @NonNull FileService fileService;

    @PostMapping
    public FileResponse uploadFile(@RequestParam("file") MultipartFile fileRequest) {
        try(InputStream in = fileRequest.getInputStream()) {
            String fileName = fileRequest.getOriginalFilename();
            long fileSize = fileRequest.getSize();
            FileEntity file = fileService.uploadFile(fileName, in, fileSize);
            return new FileResponse(file.getFileId());
        } catch (IOException e) {
            throw new EmptyFileUploadingException(e.getMessage());
        }
    }

    @GetMapping("/uploads")
    public ResponseEntity<byte[]> getFile(@RequestParam UUID fileId, HttpServletResponse response) {
        AbstractFile file = fileService.downloadFile(fileId);
        String headerValue = String.format("attachement; filename=%s", file.fileName());
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
            .body(file.content());
    }
}
