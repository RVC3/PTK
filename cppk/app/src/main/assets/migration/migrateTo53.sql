CREATE TABLE CPPKTicketReSign (		
_id             INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
EventId         INTEGER NOT NULL,
TicketNumber    INTEGER NOT NULL,
SaleDateTime    DATETIME NOT NULL,
TicketDeviceId  TEXT NOT NULL,
EDSKeyNumber    INTEGER  NOT NULL,
ReSignDateTime  DATETIME NOT NULL,
FOREIGN KEY(EventId) REFERENCES Event(_id))