CREATE TABLE `BankTransactionCashRegisterEventTemp` (
	`_id`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	`TransactionId`	INTEGER NOT NULL,
	`EventId`	INTEGER NOT NULL,
	`CashRegisterWorkingShiftId`	INTEGER NOT NULL,
	`TerminalDayId`	INTEGER NOT NULL,
	`MonthId`	INTEGER NOT NULL,
	`PointOfSaleNumber`	TEXT,
	`MerchantId`	TEXT,
	`BankCode`	INTEGER,
	`OperationType`	INTEGER NOT NULL,
	`OperationResult`	INTEGER NOT NULL,
	`Rrn`	TEXT,
	`AuthorizationCode`	TEXT,
	`SmartCardApplicationName`	TEXT,
	`CardPan`	TEXT,
	`CardEmitentName`	TEXT,
	`BankCheckNumber`	INTEGER,
	`TransactionDateTime`	DATETIME NOT NULL,
	`Total`	REAL NOT NULL,
	`CurrencyCode`	TEXT,
	`TerminalNumber`	TEXT,
	`Status`	INTEGER NOT NULL,

	FOREIGN KEY(`EventId`) REFERENCES `Event`(`_id`),
	FOREIGN KEY(`CashRegisterWorkingShiftId`) REFERENCES `CashRegisterWorkingShift`(`_id`),
	FOREIGN KEY(`TerminalDayId`) REFERENCES `TerminalDay`(`_id`),
	FOREIGN KEY(`MonthId`) REFERENCES `Months`(`_id`)
	);

INSERT INTO BankTransactionCashRegisterEventTemp
    SELECT * FROM BankTransactionCashRegisterEvent;

DROP TABLE BankTransactionCashRegisterEvent;

ALTER TABLE BankTransactionCashRegisterEventTemp RENAME TO BankTransactionCashRegisterEvent;