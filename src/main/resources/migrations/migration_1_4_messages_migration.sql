--liquibase formatted sql

--changeset init_messages_tables:1.4

CREATE TABLE messages
(
    message_id varchar(64) PRIMARY KEY,
    chat_room_id uuid NOT NULL,
    sender_id uuid NOT NULL,
    content varchar(1000) NOT NULL,
    send_date date NOT NULL,
    FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

--rollback DROP TABLE messages CASCADE;