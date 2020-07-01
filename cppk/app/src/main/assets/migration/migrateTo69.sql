DROP TABLE IF EXISTS PtkCommonKeyValueSettings;


CREATE TABLE PtkCommonKeyValueSettings(
	[name] TEXT PRIMARY KEY UNIQUE NOT NULL DEFAULT "UNKNOWN"
	, [value] TEXT
);

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_reportOpenShift",  Value.ppReportOpenShift
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_reportCloseShift",  Value.ppReportCloseShift
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_reportCloseMonth",  Value.ppReportCloseMonth
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_testPdPrintReq",  Value.pTestPdPrintReq
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_discountShiftSheetOpeningShift",  Value.pDiscountShiftSheetOpeningShift
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_discountShiftSheetClosingShiftReq",  Value.pDiscountShiftSheetClosingShiftReq
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_sheetShiftCloseShiftReq",  Value.pSheetShiftCloseShiftReq
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_sheetBlankingShiftClosingShiftReq",  Value.pSheetBlankingShiftClosingShiftReq
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_discountMonthShiftSheetClosingMonthReq",  Value.pDiscountMonthShiftSheetClosingMonthReq
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_monthSheetClosingMonthReq",  Value.pMonthSheetClosingMonthReq
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_sheetBlankingMonthClosingMonthReq",  Value.pSheetBlankingMonthClosingMonthReq
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_btMonthlySheetClosingMonthReq",  Value.pBTMonthlySheetClosingMonthReq
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_timeChangesPeriod",  Value.timeChangesPeriod
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_enableAnnulateAfterTimeOver",  Value.enableAnnulateAfterTimeOver
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_termStoragePd",  Value.termStoragePd
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_durationOfPdNextDay",  Value.durationOfPdNextDay
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_maxTimeAgoMark",  Value.maxTimeAgoMark
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_timeElectronicRegistration",  Value.timeElectronicRegistration
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_bankCode",  Value.bankCode
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_carrierName",  Value.carrierName
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_allowedStationsCodes",  Value.allowedStationsCodes
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_screenOffTimeout",  Value.screenOffTimeout
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_posTerminalCheckPeriod",  Value.posTerminalCheckPeriod
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_autoCloseTime",  Value.autoCloseTime
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_selectDraftNsi",  Value.selectDraftNsi
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_logFullSQL",  Value.logFullSQL
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_autoBlockingTimeout",  Value.autoBlockingTimeout
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_autoBlockingEnabled",  Value.autoBlockingEnabled
	FROM PtkSettingsCommon AS Value
	;

INSERT INTO PtkCommonKeyValueSettings([name], [value])
	SELECT "_timeZoneOffset",  Value.timeZoneOffset
	FROM PtkSettingsCommon AS Value
	;

UPDATE PtkCommonKeyValueSettings
	SET name = SUBSTR(name, 2)
	;

DROP TABLE IF EXISTS PtkSettingsCommon;

ALTER TABLE PtkCommonKeyValueSettings RENAME TO PtkSettingsCommon;