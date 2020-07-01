CREATE TABLE CPPKServiceSale (
    _id                             INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    EventId                         INTEGER NOT NULL,
	CashRegisterWorkingShiftId 		INTEGER NOT NULL,
    CheckId					        INTEGER,
    PriceId					        INTEGER NOT NULL,
    ServiceFeeCode			        INTEGER NOT NULL,
    ServiceFeeName		        	TEXT NOT NULL,
    SaleDateTime                    DATETIME NOT NULL,

    FOREIGN KEY(EventId) REFERENCES Event(_id),
	FOREIGN KEY(CashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id),
    FOREIGN KEY(CheckId) REFERENCES CheckTable(_id),
    FOREIGN KEY(PriceId) REFERENCES Price(_id)
);
