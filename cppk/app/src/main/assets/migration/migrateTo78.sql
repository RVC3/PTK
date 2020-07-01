CREATE TABLE SentEvents (

    SentShiftEvents                  DATETIME NOT NULL DEFAULT 0,
    SentTicketControls               DATETIME NOT NULL DEFAULT 0,
    SentTicketSales                  DATETIME NOT NULL DEFAULT 0,
    SentTestTickets                  DATETIME NOT NULL DEFAULT 0,
    SentTicketReturns                DATETIME NOT NULL DEFAULT 0,
    SentMonthClosures                DATETIME NOT NULL DEFAULT 0,
    SentTicketPaperRolls             DATETIME NOT NULL DEFAULT 0,
    SentBankTransactions             DATETIME NOT NULL DEFAULT 0,
    SentTicketReSigns                DATETIME NOT NULL DEFAULT 0,
    SentServiceSales                 DATETIME NOT NULL DEFAULT 0

);
INSERT INTO SentEvents (SentBankTransactions) VALUES (0);
