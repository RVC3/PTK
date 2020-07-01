ALTER TABLE PtkSettingsCommon ADD COLUMN posTerminalCheckPeriod INTEGER;
UPDATE PtkSettingsCommon SET posTerminalCheckPeriod = 5;