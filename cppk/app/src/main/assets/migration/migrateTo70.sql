CREATE TABLE CPPKServiceSale_temp1 (
    _id                             INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    EventId                         INTEGER NOT NULL,
	CashRegisterWorkingShiftId 		INTEGER NOT NULL,
	TicketTapeEventId               INTEGER NULL,
    CheckId					        INTEGER NOT NULL,
    PriceId					        INTEGER NOT NULL,
    ServiceFeeCode			        INTEGER NOT NULL,
    ServiceFeeName		        	TEXT NOT NULL,
    SaleDateTime                    DATETIME NOT NULL,

    FOREIGN KEY(EventId) REFERENCES Event(_id),
	FOREIGN KEY(CashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id),
    FOREIGN KEY(CheckId) REFERENCES CheckTable(_id),
    FOREIGN KEY(PriceId) REFERENCES Price(_id)
);

-- До этой версии БД в базе не должно быть событий CPPKServiceSale.
-- Грохаем без лишних телодвижений.
-- INSERT INTO CPPKServiceSale_temp1 SELECT * FROM CPPKServiceSale
DROP TABLE CPPKServiceSale;
ALTER TABLE CPPKServiceSale_temp1 RENAME TO CPPKServiceSale;
