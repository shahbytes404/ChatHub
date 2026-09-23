ALTER TABLE users
    DROP column enabled_at,
    ADD COLUMN enabled BOOLEAN NOT NULL,
    ADD COLUMN created_at timestamp not null,
    ALTER column updated_at TYPE timestamp
        using updated_at::timestamp;
