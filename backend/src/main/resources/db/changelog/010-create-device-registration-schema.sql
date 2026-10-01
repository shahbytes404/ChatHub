create table device_registration
(
    id                    uuid primary key,
    user_id               uuid         not null references users (id) on delete cascade,
    device_id             varchar(100) not null,
    platform              varchar(20)  not null,
    push_token            varchar(500),
    notifications_enabled boolean      not null default true,
    last_seen_at          timestamp    not null,
    created_at            timestamp    not null,
    unique (user_id, device_id)
);