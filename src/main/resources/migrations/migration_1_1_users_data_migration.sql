--liquibase formatted sql

--changeset init_users_data_table:1.1

CREATE TABLE users_data
(
    id UUID PRIMARY KEY,
    email varchar(50) NOT NULL UNIQUE,
    login varchar(50) NOT NULL UNIQUE,
    password varchar(257) NOT NULL,
    is_activated boolean,
    description varchar(1000),
    phone_number varchar(17) UNIQUE,
    url_tag varchar(20) UNIQUE,
    token varchar(100) UNIQUE
);

--rollback DROP TABLE users_data;