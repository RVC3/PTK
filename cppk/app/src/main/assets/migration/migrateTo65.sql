ALTER TABLE PtkSettingsCommon ADD COLUMN autoBlockingTimeout BOOLEAN NOT NULL DEFAULT 0;
ALTER TABLE PtkSettingsCommon ADD COLUMN autoBlockingEnabled BOOLEAN NOT NULL DEFAULT 0;
UPDATE PtkSettingsCommon SET autoBlockingTimeout = 30;
UPDATE PtkSettingsCommon SET autoBlockingEnabled = 1;