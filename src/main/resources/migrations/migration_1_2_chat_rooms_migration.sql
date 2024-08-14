--liquibase formatted sql

--changeset init_chat_rooms_and_messages_tables:1.2

CREATE TABLE chat_rooms
(
    id uuid PRIMARY KEY,
    creator_id uuid,
    name varchar(64),
    description TEXT,
    FOREIGN KEY (creator_id) REFERENCES users(id)
);

--rollback DROP TABLE chat_rooms;