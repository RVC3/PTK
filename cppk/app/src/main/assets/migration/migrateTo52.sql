ALTER TABLE PtkSettingsCommon ADD COLUMN screenOffTimeout INTEGER;
UPDATE PtkSettingsCommon SET screenOffTimeout = 1000 * 60 * 5;