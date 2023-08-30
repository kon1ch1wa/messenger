--liquibase formatted sql

--changeset init_users_data_table:1.1

CREATE TABLE users_data
(
    id UUID PRIMARY KEY,
    email varchar(50) NOT NULL UNIQUE,
    login varchar(50) NOT NULL UNIQUE,
    password varchar(257) NOT NULL,
    description varchar(1000),
    phone_number varchar(11) UNIQUE,
    url_tag varchar(20)
);

--rollback DROP TABLE users_data;