UPDATE TicketSaleReturnEventBase SET FeeId = NULL WHERE FeeId IN (SELECT _id FROM Fee WHERE Total=0 AND Nds=0 AND FeeType IS NULL);
DELETE FROM Fee WHERE Total=0 AND Nds=0 AND FeeType IS NULL;