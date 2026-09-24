create table messages
(
    id                uuid primary key,
    conversation_id   uuid          not null references conversations (id) on delete cascade,
    sender_id         uuid          not null references users (id),
    client_message_id varchar(100)  not null,
    sequence_number   bigint        not null,
    message_type      varchar(20)   not null,
    content           varchar(4000) not null,
    created_at        timestamp     not null,

    UNIQUE (sender_id, client_message_id),
    UNIQUE (conversation_id, sequence_number)
);

-- missed messages = where conversation_id = ? and sequence_number > ?
-- where conversation_id = ? order by sequence_number DESC

create index idx_message_conversation_sequence on messages (conversation_id, sequence_number DESC);