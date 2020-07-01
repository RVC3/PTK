ALTER TABLE PtkSettingsPrivate ADD COLUMN isPosEnabled BOOLEAN;
ALTER TABLE PtkSettingsPrivate ADD COLUMN isSaleEnabled BOOLEAN;
UPDATE PtkSettingsPrivate SET isPosEnabled = 1, isSaleEnabled = 1 WHERE isDefault = 1;