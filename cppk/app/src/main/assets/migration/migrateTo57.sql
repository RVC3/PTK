ALTER TABLE TerminalDay ADD COLUMN TerminalNumber TEXT NOT NULL DEFAULT "0";
UPDATE TerminalDay SET TerminalNumber =
    CASE WHEN (SELECT count(*) from BankTransactionCashRegisterEvent) > 0
       THEN (SELECT TerminalNumber from BankTransactionCashRegisterEvent ORDER BY _id desc limit 1 )
       ELSE "0"
       END;