ALTER TABLE PtkSettingsCommon ADD COLUMN timeZoneOffset INTEGER NOT NULL default 0;
UPDATE PtkSettingsCommon SET timeZoneOffset = 10800000;