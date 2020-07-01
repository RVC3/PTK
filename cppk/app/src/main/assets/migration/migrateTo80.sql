CREATE TABLE FineSaleEvent (
    _id                             INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    Amount                          TEXT NOT NULL,
    OperationDateTime               INTEGER NOT NULL,
    PaymentMethodCode               INTEGER NOT NULL,
    EventId                         INTEGER NOT NULL,
	CashRegisterWorkingShiftId      INTEGER NOT NULL,
	TicketTapeEventId               INTEGER NULL,
    CheckId                         INTEGER NULL,
    BankTransactionEventId          INTEGER NULL,
    FineCode                        INTEGER NOT NULL,
    Status                          INTEGER NOT NULL,

    FOREIGN KEY(EventId) REFERENCES Event(_id),
	FOREIGN KEY(CashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id),
    FOREIGN KEY(TicketTapeEventId) REFERENCES TicketTapeEvent(_id),
    FOREIGN KEY(CheckId) REFERENCES CheckTable(_id),
    FOREIGN KEY(BankTransactionEventId) REFERENCES BankTransactionCashRegisterEvent(_id)
);
