-- Создание временной таблицы со списком BankTransactionCashRegisterEvent, которые надо удалить
CREATE TEMP TABLE TransactionForDelete (
    Id INTEGER
);
-- Создание временной таблицы со списком Event, которые надо удалить
CREATE TEMP TABLE EventForDelete (
    Id INTEGER
);
-- Создание временной таблицы со списком StationDevice, которые надо удалить
CREATE TEMP TABLE StationDeviceForDelete (
    Id INTEGER
);

-- Получение BankTransactionCashRegisterEvent, которые надо удалить
INSERT INTO TransactionForDelete
WITH
  -- Максимальный Id
  MaxId(Id) AS (
     SELECT MAX(_id) AS Id FROM BankTransactionCashRegisterEvent
  ),
  -- Транзакция продажи, которую надо удалить
  SaleTransaction(Id) AS (
     SELECT T._id FROM BankTransactionCashRegisterEvent AS T, MaxId WHERE TransactionId = 0 AND T.OperationType = 1 AND OperationResult = 1 AND (T._id = MaxId.Id OR T._id = MaxId.Id - 1)
  ),
  -- Транзакция отмены, которую надо удалить
  CancelTransaction(Id) AS (
     SELECT T._id FROM BankTransactionCashRegisterEvent AS T, MaxId, SaleTransaction WHERE T.OperationType = 2 AND T._id = SaleTransaction.Id + 1 AND T._id = MaxId.Id
  )
  SELECT * FROM SaleTransaction UNION SELECT * FROM CancelTransaction;
-- Получение Event, которые надо удалить
INSERT INTO EventForDelete
SELECT EventId FROM BankTransactionCashRegisterEvent WHERE _id IN (SELECT * FROM TransactionForDelete);
-- Получение StationDevice, которые надо удалить
INSERT INTO StationDeviceForDelete
SELECT StationDeviceId FROM Event WHERE _id IN (SELECT * FROM EventForDelete);

-- Удаление BankTransactionCashRegisterEvent
DELETE FROM BankTransactionCashRegisterEvent WHERE _id IN (SELECT * FROM TransactionForDelete);
-- Удаление Event
DELETE FROM Event WHERE _id IN (SELECT * FROM EventForDelete);
-- Удаление StationDevice
DELETE FROM StationDevice WHERE _id IN (SELECT * FROM StationDeviceForDelete);

-- Удаление временной таблицы со списком BankTransactionCashRegisterEvent, которые надо удалить
DROP TABLE TransactionForDelete;
-- Удаление временной таблицы со списком Event, которые надо удалить
DROP TABLE EventForDelete;
-- Удаление временной таблицы со списком StationDevice, которые надо удалить
DROP TABLE StationDeviceForDelete;