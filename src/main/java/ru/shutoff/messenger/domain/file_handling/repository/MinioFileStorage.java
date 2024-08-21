package ru.shutoff.messenger.domain.file_handling.repository;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.shutoff.messenger.domain.file_handling.exception.InvalidFileDataException;
import ru.shutoff.messenger.domain.file_handling.exception.InvalidMinioCredentialsException;

@NoArgsConstructor
@Setter
@Slf4j
public class MinioFileStorage {
    private String bucketName;

    private Integer partSize;

    private MinioClient minioClient;

    public void store(String fileName, InputStream fileBody) {
        PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(fileBody, -1, partSize).build();
        try {
            minioClient.putObject(args);
        } catch (MinioException e) {
            log.error("Error while uploading file: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            log.error("Error while uploading file: {}", e.getMessage());
            throw new InvalidMinioCredentialsException(e.getMessage());
        } catch (IOException e) {
            log.error("Error while uploading file: {}", e.getMessage());
            throw new InvalidFileDataException(e.getMessage());
        }
    }

    public byte[] get(String fileName, long size) {
        GetObjectArgs args = GetObjectArgs.builder().bucket(bucketName).object(fileName).build();
        try (GetObjectResponse resp = minioClient.getObject(args)) {
            byte[] bytes = resp.readAllBytes();
            log.info(bytes.length + "");
            log.info(bytes.toString());
            log.info(bytes.length + "");
            log.info(bytes.toString());
            return bytes;
        } catch (MinioException e) {
            log.error("Error while downloading file: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            log.error("Error while downloading file: {}", e.getMessage());
            throw new InvalidMinioCredentialsException(e.getMessage());
        } catch (IOException e) {
            log.error("Error while downloading file: {}", e.getMessage());
            throw new InvalidFileDataException(e.getMessage());
        }
    }
}
