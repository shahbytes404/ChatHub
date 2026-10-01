create table outbox_events
(
    id             uuid primary key,
    aggregate_type varchar(40) not null,
    aggregate_id   uuid        not null,
    event_type     varchar(60) not null,
    payload_json   text        not null,
    status         varchar(20) not null,
    attempts       integer     not null default 0,
    available_at   timestamp   not null,
    created_at     timestamp   not null,
    published_at   timestamp
);

-- where status = 'PENDING' and available_at <= now() order by created_at
create index idx_outbox_ready on outbox_events (status, available_at, created_at);

create table processed_events
(
    event_id     uuid primary key,
    processed_at timestamp not null
)