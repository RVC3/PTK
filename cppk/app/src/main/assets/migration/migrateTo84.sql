CREATE TABLE CouponReadEvent (
    _id                        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    EventId                    INTEGER  NOT NULL,
    CashRegisterWorkingShiftId INTEGER  NOT NULL,
    PreTicketNumber            INTEGER NOT NULL,
    PrintDateTime              INTEGER NOT NULL,
    DeviceId                   TEXT NOT NULL,
    StationCode                INTEGER NOT NULL,
    PtsKeyId                   TEXT NOT NULL,
    Status                     INTEGER NOT NULL,

    FOREIGN KEY(EventId) REFERENCES Event(_id),
    FOREIGN KEY(CashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id)
);
ALTER TABLE CPPKTicketSales ADD COLUMN CouponReadEventId INTEGER REFERENCES CouponReadEvent(_id);