-- AuditTrailEvent
CREATE TABLE TempTable AS SELECT * FROM AuditTrailEvent;
DROP TABLE AuditTrailEvent;
CREATE TABLE AuditTrailEvent (
    _id                        INTEGER  NOT NULL
                                        PRIMARY KEY AUTOINCREMENT
                                        UNIQUE,
    Type                       INTEGER  NOT NULL,
    ExtEventId                 INTEGER  NOT NULL,
    OperationTime              DATETIME NOT NULL,
    CashRegisterWorkingShiftId INTEGER,
    MonthEventId               INTEGER  NOT NULL,
    FOREIGN KEY (
        MonthEventId
    )
    REFERENCES MonthEvent (_id),
    FOREIGN KEY (
        CashRegisterWorkingShiftId
    )
    REFERENCES CashRegisterWorkingShift (_id)
);
INSERT INTO AuditTrailEvent SELECT * FROM TempTable;
DROP TABLE TempTable;

-- BankTransactionCashRegisterEvent
CREATE TABLE TempTable AS SELECT * FROM BankTransactionCashRegisterEvent;
DROP TABLE BankTransactionCashRegisterEvent;
CREATE TABLE BankTransactionCashRegisterEvent (
    _id                        INTEGER  NOT NULL
                                        PRIMARY KEY AUTOINCREMENT
                                        UNIQUE,
    TransactionId              INTEGER  NOT NULL,
    EventId                    INTEGER  NOT NULL,
    CashRegisterWorkingShiftId INTEGER  NOT NULL,
    TerminalDayId              INTEGER  NOT NULL,
    MonthId                    INTEGER  NOT NULL,
    PointOfSaleNumber          TEXT,
    MerchantId                 TEXT,
    BankCode                   INTEGER,
    OperationType              INTEGER  NOT NULL,
    OperationResult            INTEGER  NOT NULL,
    Rrn                        TEXT,
    AuthorizationCode          TEXT,
    SmartCardApplicationName   TEXT,
    CardPan                    TEXT,
    CardEmitentName            TEXT,
    BankCheckNumber            INTEGER,
    TransactionDateTime        DATETIME NOT NULL,
    Total                      REAL     NOT NULL,
    CurrencyCode               TEXT,
    TerminalNumber             TEXT,
    Status                     INTEGER  NOT NULL,
    FOREIGN KEY (
        EventId
    )
    REFERENCES Event (_id),
    FOREIGN KEY (
        CashRegisterWorkingShiftId
    )
    REFERENCES CashRegisterWorkingShift (_id),
    FOREIGN KEY (
        TerminalDayId
    )
    REFERENCES TerminalDay (_id),
    FOREIGN KEY (
        MonthId
    )
    REFERENCES MonthEvent (_id)
);
INSERT INTO BankTransactionCashRegisterEvent SELECT * FROM TempTable;
DROP TABLE TempTable;

-- PrintReportEvent
CREATE TABLE TempTable AS SELECT * FROM PrintReportEvent;
DROP TABLE PrintReportEvent;
CREATE TABLE PrintReportEvent (
    _id                        INTEGER  NOT NULL
                                        PRIMARY KEY
                                        UNIQUE,
    CashRegisterEventId        INTEGER  NOT NULL,
    CashRegisterWorkingShiftId INTEGER,
    MonthEventId               INTEGER  NOT NULL,
    EventId                    INTEGER  NOT NULL,
    ReportType                 INTEGER,
    TicketTapeEventId          INTEGER  NOT NULL,
    OperationTime              DATETIME NOT NULL,
    CashInFR                   TEXT     NOT NULL,
    FOREIGN KEY (
        CashRegisterEventId
    )
    REFERENCES CashRegisterEvent (_id),
    FOREIGN KEY (
        EventId
    )
    REFERENCES Event (_id),
    FOREIGN KEY (
        MonthEventId
    )
    REFERENCES MonthEvent (_id),
    FOREIGN KEY (
        CashRegisterWorkingShiftId
    )
    REFERENCES CashRegisterWorkingShift (_id)
);
INSERT INTO PrintReportEvent SELECT * FROM TempTable;
DROP TABLE TempTable;