CREATE TABLE CashRegisterWorkingShiftTemp (
    _id                     INTEGER  NOT NULL
                                     PRIMARY KEY AUTOINCREMENT
                                     UNIQUE,
    EventId                 INTEGER  NOT NULL,
    CashRegisterEventId     INTEGER  NOT NULL,
    ShiftId                 TEXT     NOT NULL,
    MonthEventId            INTEGER  NOT NULL,
    ShiftStartDateTime      DATETIME NOT NULL,
    ShiftEndDateTime        DATETIME,
    OperationDateTime       DATETIME NOT NULL,
    ShiftStatus             INTEGER  NOT NULL,
    ProgressStatus          INTEGER  NOT NULL,
    Number                  INTEGER  NOT NULL,
    PaperConsumption        INTEGER  NOT NULL,
    IsPaperCounterRestarted BOOLEAN  NOT NULL,
    CashInFR                TEXT,
    CheckId                 INTEGER,
    FOREIGN KEY (
        CashRegisterEventId
    )
    REFERENCES CashRegisterEvent (_id),
    FOREIGN KEY (
        MonthEventId
    )
    REFERENCES MonthEvent (_id),
    FOREIGN KEY (
        EventId
    )
    REFERENCES Event (_id),
    FOREIGN KEY (
        CheckId
    )
    REFERENCES CheckTable (_id)
);

INSERT INTO CashRegisterWorkingShiftTemp (
                                _id,
                                EventId,
                                CashRegisterEventId,
                                ShiftId,
                                MonthEventId,
                                ShiftStartDateTime,
                                ShiftEndDateTime,
                                OperationDateTime,
                                ShiftStatus,
                                Number,
                                PaperConsumption,
                                IsPaperCounterRestarted,
                                CashInFR,
                                ProgressStatus
)
SELECT  _id,
        EventId,
        CashRegisterEventId,
        ShiftId,
        MonthEventId,
        ShiftStartDateTime,
        ShiftEndDateTime,
        OperationDateTime,
        Status,
        Number,
        PaperConsumption,
        IsPaperCounterRestarted,
        CashInFR,
        5
FROM CashRegisterWorkingShift;

DROP TABLE CashRegisterWorkingShift;

ALTER TABLE CashRegisterWorkingShiftTemp RENAME TO CashRegisterWorkingShift;
