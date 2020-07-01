ALTER TABLE PtkSettingsCommon ADD COLUMN allowedStationsCodes TEXT;
UPDATE PtkSettingsCommon SET allowedStationsCodes = NULL;