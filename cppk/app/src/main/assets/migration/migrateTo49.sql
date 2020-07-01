ALTER TABLE PtkSettingsPrivate ADD COLUMN isUseMobileData BOOLEAN;
UPDATE PtkSettingsPrivate SET isUseMobileData = 0 WHERE isDefault = 1;