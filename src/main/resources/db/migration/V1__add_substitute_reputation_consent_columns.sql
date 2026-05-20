ALTER TABLE notification_consents
    ADD COLUMN IF NOT EXISTS substitute_notification_consent BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS reputation_notification_consent BOOLEAN NOT NULL DEFAULT true;

UPDATE notification_consents
SET substitute_notification_consent = notification_consent,
    reputation_notification_consent = notification_consent
WHERE notification_consent = false;

ALTER TABLE notification_consents
    ALTER COLUMN substitute_notification_consent DROP DEFAULT,
    ALTER COLUMN reputation_notification_consent DROP DEFAULT;
