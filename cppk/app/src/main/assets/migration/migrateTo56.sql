ALTER TABLE PtkSettingsCommon ADD COLUMN selectDraftNsi BOOLEAN;
UPDATE PtkSettingsCommon SET selectDraftNsi = 0;