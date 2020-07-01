UPDATE PtkSettingsPrivate SET terminalNumber = CASE WHEN terminalNumber < 0 THEN terminalNumber + 4294967296 ELSE terminalNumber END;
UPDATE CPPKTicketControl SET EdsKeyNumber = CASE WHEN EdsKeyNumber < 0 THEN EdsKeyNumber + 4294967296 ELSE EdsKeyNumber END;
UPDATE CPPKTicketControl SET SellTicketDeviceId = CASE WHEN SellTicketDeviceId < 0 THEN SellTicketDeviceId + 4294967296 ELSE SellTicketDeviceId END;
UPDATE CPPKTicketReSign SET EDSKeyNumber = CASE WHEN EDSKeyNumber < 0 THEN EDSKeyNumber + 4294967296 ELSE EDSKeyNumber END;
UPDATE CPPKTicketReSign SET TicketDeviceId = CASE WHEN TicketDeviceId < 0 THEN TicketDeviceId + 4294967296 ELSE TicketDeviceId END;
UPDATE ParentTicketInfo SET CashRegisterNumber = CASE WHEN CashRegisterNumber < 0 THEN CashRegisterNumber + 4294967296 ELSE CashRegisterNumber END;
UPDATE StationDevice SET DeviceId = CASE WHEN DeviceId < 0 THEN DeviceId + 4294967296 ELSE DeviceId END;
UPDATE CPPKTicketSales SET EDSKeyNumber = CASE WHEN EDSKeyNumber < 0 THEN EDSKeyNumber + 4294967296 ELSE EDSKeyNumber END;