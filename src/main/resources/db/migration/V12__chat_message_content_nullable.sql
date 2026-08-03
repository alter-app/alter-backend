-- 이미지-only 메시지 허용을 위해 content NOT NULL 제약 완화.
ALTER TABLE chat_messages ALTER COLUMN content DROP NOT NULL;
