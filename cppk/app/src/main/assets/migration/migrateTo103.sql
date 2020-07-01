ALTER TABLE TicketEventBase ADD COLUMN StartDayOffset INTEGER DEFAULT NULL;

UPDATE TicketEventBase
SET StartDayOffset = CASE
                         WHEN SaleDateTime < ValidFromDateTime
                         THEN ROUND((ValidFromDateTime - SaleDateTime) / 86400 + 0.5)
                         ELSE 0
                     END;