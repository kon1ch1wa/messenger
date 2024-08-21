package ru.shutoff.messenger.domain.file_handling.controller;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import ru.shutoff.messenger.MessengerApplication;
import ru.shutoff.messenger.domain.file_handling.configuration.TestConfiguration;
import ru.shutoff.messenger.domain.file_handling.dto.FileResponse;
import ru.shutoff.messenger.domain.file_handling.model.FileEntity;
import ru.shutoff.messenger.domain.file_handling.repository.FileInfoRepository;
import ru.shutoff.messenger.domain.file_handling.repository.MinioFileStorage;

@Testcontainers
@SpringBootTest(classes = MessengerApplication.class)
@Import(TestConfiguration.class)
@AutoConfigureMockMvc
@Slf4j
public class FileUploadsControllerTest {
    @Container
    public static MinIOContainer minio = new MinIOContainer("minio/minio")
            .withExposedPorts(9000)
            .withUserName("minioadmin")
            .withPassword("minioadmin");

	@Container
	public static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
			.withUsername("admin")
			.withPassword("admin")
			.withDatabaseName("messenger_db");

    @Autowired
    private MinioFileStorage minioFileStorage;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileInfoRepository fileInfoRepository;

	@DynamicPropertySource
	static void registerProps(DynamicPropertyRegistry registry) {
		registry.add("minio.endpoint", () -> {return String.format("http://localhost:%s", minio.getMappedPort(9000));});
		registry.add("minio.accessKey", minio::getUserName);
		registry.add("minio.secretKey", minio::getPassword);
		registry.add("minio.bucketName", () -> {return "test";});

		registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgresContainer::getUsername);
		registry.add("spring.datasource.password", postgresContainer::getPassword);
		registry.add("spring.liquibase.url", postgresContainer::getJdbcUrl);
		registry.add("spring.liquibase.user", postgresContainer::getUsername);
		registry.add("spring.liquibase.password", postgresContainer::getPassword);
	}

    @Test
    public void containerIsRunningTest() {
        log.info(String.format("Endpoint: localhost:%s", minio.getMappedPort(9000)));
        log.info(String.format("AccessKey: %s", minio.getUserName()));
        log.info(String.format("SecretKey: %s", minio.getPassword()));
        log.info(String.format("Host: %s", minio.getHost()));
        assertTrue(minio.isRunning());
        assertTrue(postgresContainer.isRunning());
    }

    @Test
    public void uploadFileTest() {
        doNothing().when(fileInfoRepository).save(any(FileEntity.class));
        InputStream fileBody = new ByteArrayInputStream("This is test body".getBytes());
        try {
            MockMultipartFile file = new MockMultipartFile("file", fileBody);
    
            String content = mockMvc.perform(multipart(HttpMethod.POST, "/file").file(file))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            FileResponse resp = new ObjectMapper().readValue(content, FileResponse.class);
            assertNotNull(resp.fileId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void uploadIncorrectFileTest() {
        doNothing().when(fileInfoRepository).save(any(FileEntity.class));
        try {
            MockMultipartFile file = mock(MockMultipartFile.class);
            when(file.getInputStream()).thenThrow(new IOException("Test exception"));
            mockMvc.perform(multipart("/file").file(file))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void downloadFileTest() {
        byte[] expectedContent = "This is test body".getBytes();
        ByteArrayInputStream fileBody = new ByteArrayInputStream(expectedContent);
        minioFileStorage.store("TestFile_2.txt", fileBody);

        UUID testId = UUID.randomUUID();
        FileEntity file = new FileEntity(testId, "TestFile_2.txt", Long.valueOf(0));
        when(fileInfoRepository.get(testId)).thenReturn(file);

        try {
            byte[] actualContent = mockMvc.perform(get("/file/uploads").param("fileId", testId.toString()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsByteArray();
            assertArrayEquals(expectedContent, actualContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
