DROP TABLE IF EXISTS TicketEventBaseTemp;
CREATE TABLE TicketEventBaseTemp (
    _id                        INTEGER  NOT NULL
                                        PRIMARY KEY AUTOINCREMENT
                                        UNIQUE,
    CashRegisterWorkingShiftId INTEGER  NOT NULL,
    SaleDateTime               DATETIME,
    ValidFromDateTime          DATETIME,
    ValidTillDateTime          DATETIME,
    DepartureStationCode       INTEGER  NOT NULL,
    DestinationStationCode     INTEGER  NOT NULL,
    TariffCode                 INTEGER  NOT NULL,
    WayType                    INTEGER  NOT NULL,
    Type                       TEXT,
    TypeCode                   INTEGER  NOT NULL,
    SmartCardId                INTEGER,
    TicketCategoryCode         INTEGER  NOT NULL,
    TicketTypeShortName        TEXT     NOT NULL,
    FOREIGN KEY (
        CashRegisterWorkingShiftId
    )
    REFERENCES CashRegisterWorkingShift (_id),
    FOREIGN KEY (
        SmartCardId
    )
    REFERENCES SmartCard (_id)
);


INSERT INTO TicketEventBaseTemp
    SELECT * FROM TicketEventBase;


DROP TABLE IF EXISTS TicketEventBase;

ALTER TABLE TicketEventBaseTemp RENAME TO TicketEventBase;