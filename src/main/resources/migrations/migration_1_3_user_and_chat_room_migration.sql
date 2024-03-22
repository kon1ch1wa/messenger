--liquibase formatted sql

--changeset init_user_and_chat_room_table:1.3

CREATE TABLE user_and_chat_room
(
    id varchar(64) PRIMARY KEY,
    sender_id varchar(64) NOT NULL,
    chat_room_id varchar(64) NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id)
);

--rollback DROP TABLE user_and_chat_room CASCADE;