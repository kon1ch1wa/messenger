--liquibase formatted sql

--changeset init_files_tables:1.5

CREATE TABLE files
(
    id serial PRIMARY KEY,
    file_id uuid NOT NULL,
    file_path varchar(1000) NOT NULL,
    file_size int NOT NULL
);

--rollback DROP TABLE files;