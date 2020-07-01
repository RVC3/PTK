DROP TABLE IF EXISTS PtkSentEventsKeyValue;


CREATE TABLE PtkSentEventsKeyValue(
	[name] TEXT PRIMARY KEY UNIQUE NOT NULL DEFAULT "UNKNOWN"
	, [value] DATETIME
);

INSERT INTO PtkSentEventsKeyValue([name], [value])
	SELECT "_SentShiftEvents",  Value.SentShiftEvents
	FROM SentEvents AS Value
	;

INSERT INTO PtkSentEventsKeyValue([name], [value])
	SELECT "_SentTicketControls",  Value.SentTicketControls
	FROM SentEvents AS Value
	;

INSERT INTO PtkSentEventsKeyValue([name], [value])
	SELECT "_SentTicketSales",  Value.SentTicketSales
	FROM SentEvents AS Value
	;

INSERT INTO PtkSentEventsKeyValue([name], [value])
	SELECT "_SentTestTickets",  Value.SentTestTickets
	FROM SentEvents AS Value
	;

INSERT INTO PtkSentEventsKeyValue([name], [value])
	SELECT "_SentTicketReturns",  Value.SentTicketReturns
	FROM SentEvents AS Value
	;

INSERT INTO PtkSentEventsKeyValue([name], [value])
	SELECT "_SentMonthClosures",  Value.SentMonthClosures
	FROM SentEvents AS Value
	;

INSERT INTO PtkSentEventsKeyValue([name], [value])
	SELECT "_SentTicketPaperRolls",  Value.SentTicketPaperRolls
	FROM SentEvents AS Value
	;

INSERT INTO PtkSentEventsKeyValue([name], [value])
	SELECT "_SentBankTransactions",  Value.SentBankTransactions
	FROM SentEvents AS Value
	;

INSERT INTO PtkSentEventsKeyValue([name], [value])
	SELECT "_SentTicketReSigns",  Value.SentTicketReSigns
	FROM SentEvents AS Value
	;

INSERT INTO PtkSentEventsKeyValue([name], [value])
	SELECT "_SentServiceSales",  Value.SentServiceSales
	FROM SentEvents AS Value
	;

INSERT INTO PtkSentEventsKeyValue([name], [value])
    SELECT "_SentFinePaidEvents",  0
    ;

UPDATE PtkSentEventsKeyValue
	SET name = SUBSTR(name, 2)
	;

DROP TABLE IF EXISTS SentEvents;

ALTER TABLE PtkSentEventsKeyValue RENAME TO SentEvents;