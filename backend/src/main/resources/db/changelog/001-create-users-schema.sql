CREATE TABLE users (
    id  UUID PRIMARY KEY ,
    display_name VARCHAR(80) NOT NULL,
    email VARCHAR(254) NOT NULL unique ,
    password_hash VARCHAR(100) not null,
    enabled_at timestamp not null,
    updated_at varchar(100) not null
)