package ru.shutoff.messenger.domain.file_handling.repository;

import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shutoff.messenger.domain.file_handling.model.FileEntity;

@RequiredArgsConstructor
@Component
@Slf4j
public class FileInfoRepositoryImpl implements FileInfoRepository {
    private static final String SAVE_FILE = "INSERT INTO files (file_id, file_path, file_size) VALUES (?, ?, ?)";
    private static final String GET_FILE_BY_ID = "SELECT * FROM files WHERE file_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<FileEntity> fileEntityRowMapper = (rs, rowNum) -> new FileEntity(
        rs.getObject("file_id", UUID.class),
        rs.getString("file_path"),
        rs.getLong("file_size")
    );

    @Override
    public void save(FileEntity file) {
        try {
            jdbcTemplate.update(SAVE_FILE, file.getFileId(), file.getFilePath(), file.getFileSize());
        } catch (DataAccessException e) {
            log.error("Error while saving FileEntity: {}", e.getMessage());
        }
    }

    @Override
    public FileEntity get(UUID fileId) {
        try {
            return jdbcTemplate.queryForObject(GET_FILE_BY_ID, fileEntityRowMapper, fileId);
        } catch (DataAccessException e) {
            log.error("Error while getting FileEntity: {}", e.getMessage());
            return null;
        }
    }
}
