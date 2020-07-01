CREATE TABLE TicketTapeEventTemp (
    _id                        INTEGER  NOT NULL
                                        PRIMARY KEY
                                        UNIQUE,
    EventId                    INTEGER  NOT NULL,
    CashRegisterEventId        INTEGER  NOT NULL,
    CashRegisterWorkingShiftId INTEGER,
    MonthEventId               INTEGER  NOT NULL,
    TicketTapeId               TEXT     NOT NULL,
    Series                     TEXT     NOT NULL,
    Number                     INTEGER  NOT NULL,
    StartTime                  DATETIME NOT NULL,
    EndTime                    DATETIME,
    PaperConsumption           INTEGER  NOT NULL,
    IsPaperCounterRestarted    BOOLEAN  NOT NULL,
    ExpectedFirstDocNumber     INTEGER  NOT NULL
                                        DEFAULT 0,
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

INSERT INTO TicketTapeEventTemp
    SELECT         _id,
                   EventId,
                   CashRegisterEventId,
                   CashRegisterWorkingShiftId,
                   MonthEventId,
                   TicketTapeId,
                   Series,
                   (SELECT CAST(CAST (Number AS NUMERIC) AS INT)) Number,
                   StartTime,
                   EndTime,
                   PaperConsumption,
                   IsPaperCounterRestarted,
                   ExpectedFirstDocNumber
   FROM TicketTapeEvent;

DROP TABLE TicketTapeEvent;

ALTER TABLE TicketTapeEventTemp RENAME TO TicketTapeEvent;