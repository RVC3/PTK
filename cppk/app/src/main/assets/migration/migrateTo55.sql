ALTER TABLE PtkSettingsCommon ADD COLUMN autoCloseTime INTEGER;
UPDATE PtkSettingsCommon SET autoCloseTime = 15;