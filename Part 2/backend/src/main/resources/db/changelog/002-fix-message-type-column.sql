ALTER TABLE messages
    ALTER COLUMN message_type type varchar(20)
        using case message_type
                  when 0 then 'TEXT'
                  when 1 then 'SYSTEM'
                  ELSE message_type::text
        END;