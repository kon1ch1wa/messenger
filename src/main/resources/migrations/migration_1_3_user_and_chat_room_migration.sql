--liquibase formatted sql

--changeset init_user_and_chat_room_table:1.3

CREATE TABLE user_and_chat_room
(
    id serial PRIMARY KEY,
    user_id uuid NOT NULL,
    chat_room_id uuid NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id)
);

--rollback DROP TABLE user_and_chat_room CASCADE;