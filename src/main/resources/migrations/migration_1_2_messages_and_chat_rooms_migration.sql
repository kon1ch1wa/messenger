--liquibase formatted sql

--changeset init_chat_rooms_and_messages_tables:1.2

CREATE TABLE chat_rooms
(
    id varchar(65) PRIMARY KEY,
    first_participant_id varchar(32) NOT NULL,
    second_participant_id varchar(32) NOT NULL
);

CREATE TABLE messages
(
    message_id varchar(36) PRIMARY KEY,
    content varchar(1000) NOT NULL,
    sender_id varchar(32) NOT NULL,
    receiver_id varchar(32) NOT NULL,
    chat_room_id varchar(65) NOT NULL,
    send_date date NOT NULL,
    CONSTRAINT fk_chat_room_id_chat_rooms_id FOREIGN KEY (chat_room_id) REFERENCES  chat_rooms(id)
);

--rollback DROP TABLE messages CASCADE;
--rollback DROP TABLE chat_rooms;