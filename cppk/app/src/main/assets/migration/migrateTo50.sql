ALTER TABLE PtkSettingsCommon ADD COLUMN carrierName TEXT;
UPDATE PtkSettingsCommon SET carrierName = 'АО ЦЕНТРАЛЬНАЯ ППК';