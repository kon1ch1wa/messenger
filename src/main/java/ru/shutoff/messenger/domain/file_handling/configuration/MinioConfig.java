package ru.shutoff.messenger.domain.file_handling.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shutoff.messenger.domain.file_handling.repository.MinioFileStorage;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.partSize}")
    private Integer partSize;

    @Bean
    public MinioFileStorage minioFileStorage() {
        log.info("ENDPOINT: {}", endpoint);
        log.info("ACCESS_KEY: {}", accessKey);
        log.info("SECRET_KEY: {}", secretKey);
        log.info("BUCKET_NAME: {}", bucketName);
        log.info("PART_SIZE: {}", partSize);
        MinioFileStorage minioFileStorage = new MinioFileStorage();
        minioFileStorage.setBucketName(bucketName);
        minioFileStorage.setPartSize(partSize);
        minioFileStorage.setMinioClient(minioClient());
        return minioFileStorage;
    }

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient
            .builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
        try {
            if (!minioClient.bucketExists(bucketExistsArgs)) {
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build();
                minioClient.makeBucket(makeBucketArgs);
            }
        } catch (Exception e) {
            throw new Error("Error while connecting to Minio: " + e.getMessage(), e);
        }
        return minioClient;
    }
}
