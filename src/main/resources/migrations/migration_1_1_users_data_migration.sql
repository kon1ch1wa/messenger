--liquibase formatted sql

--changeset init_users_table:1.1

CREATE TABLE users
(
    id varchar(32) PRIMARY KEY,
    email varchar(50) NOT NULL UNIQUE,
    login varchar(50) NOT NULL UNIQUE,
    name varchar(50) NOT NULL,
    password varchar(257) NOT NULL,
    is_activated boolean,
    description varchar(1000),
    phone_number varchar(17) UNIQUE,
    url_tag varchar(20) UNIQUE,
    token varchar(100) UNIQUE
);

--rollback DROP TABLE users;