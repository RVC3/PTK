CREATE TABLE TestTicketEventTemp (
    _id                        INTEGER NOT NULL
                                       PRIMARY KEY AUTOINCREMENT
                                       UNIQUE,
    EventId                    INTEGER NOT NULL,
    CashRegisterWorkingShiftId INTEGER NOT NULL,
    CheckId                    INTEGER DEFAULT NULL,
    TicketTapeEventId          INTEGER NOT NULL,
    Status                     INTEGER NOT NULL,
    FOREIGN KEY (
        CashRegisterWorkingShiftId
    )
    REFERENCES CashRegisterWorkingShift (_id),
    FOREIGN KEY (
        CheckId
    )
    REFERENCES CheckTable (_id),
    FOREIGN KEY (
        EventId
    )
    REFERENCES Event (_id)
);
INSERT INTO TestTicketEventTemp select *, 2 from TestTicketEvent;
DROP TABLE TestTicketEvent;
ALTER TABLE TestTicketEventTemp RENAME TO TestTicketEvent;