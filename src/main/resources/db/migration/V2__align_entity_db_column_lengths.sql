-- C-2: email_send_logs.email  varchar(255) -> varchar(100)
ALTER TABLE email_send_logs
    ALTER COLUMN email TYPE varchar(100);

-- D-1: user_certificates.type  text -> varchar(24)
ALTER TABLE user_certificates
    ALTER COLUMN type TYPE varchar(24);

-- D-2: reputation_keyword_map.description  text -> varchar(128)
ALTER TABLE reputation_keyword_map
    ALTER COLUMN description TYPE varchar(128);
