-- Добавим колонку
ALTER TABLE TicketTapeEvent ADD COLUMN ExpectedFirstDocNumber INTEGER NOT NULL DEFAULT 0;

-- Установим значения на основании времени
UPDATE TicketTapeEvent
SET ExpectedFirstDocNumber =
  (SELECT
     (SELECT CASE WHEN MAX(CheckTable.SerialNumber) IS NULL THEN 1 ELSE MAX(CheckTable.SerialNumber) + 1 END
      FROM CheckTable
      WHERE CheckTable.PrintDateTime < TicketTapeEvent.StartTime)
   FROM TicketTapeEvent AS T
   WHERE _id = T._id);