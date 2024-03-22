--liquibase formatted sql

--changeset init_chat_rooms_and_messages_tables:1.2

CREATE TABLE chat_rooms
(
    id varchar(64) PRIMARY KEY,
    name varchar(64),
    description TEXT
);

--rollback DROP TABLE chat_rooms;