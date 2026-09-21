create table audit_events
(
    id            uuid primary key,
    actor_id      uuid,
    action        varchar(80) not null,
    resource_type varchar(40) not null,
    resource_id   varchar(100),

    metadata_json text        not null,
    occurred_at   timestamp   not null
);

create index idx_audit_actor_time on audit_events (actor_id, occurred_at desc);