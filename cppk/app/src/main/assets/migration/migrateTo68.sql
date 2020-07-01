-- Создаем таблицу для хранения счетчиков билетной ленты
CREATE TABLE PaperUsage (
    _id                 INTEGER,
    PrevOdometerValue   INTEGER,
    PaperLength         INTEGER,
    IsRestarted         BOOLEAN
);

-- Добавляем 2 стандартных счетчика
-- Один в разрезе по смене, другой в разрезе по билетной ленте
-- Последнее показние одометра берем из событий открытия смены или установки билетной ленты,
-- чтобы иметь право установить PaperLength = 0
INSERT INTO PaperUsage
VALUES (0,
          (SELECT PrinterOdometerValue
           FROM CashRegisterWorkingShift
           WHERE Status = 0
           ORDER BY _id DESC LIMIT 1), 0,
                                       0), (1,
                                              (SELECT PrinterOdometerValue
                                               FROM TicketTapeEvent
                                               WHERE EndTime IS NULL
                                               ORDER BY _id DESC LIMIT 1), 0,
                                                                           0);
-- Создаем новый вариант таблицы CashRegisterWorkingShift:
-- Удалена колонка PrinterOdometerValue
-- Добавлены колонки PaperConsumption и IsPaperCounterRestarted
CREATE TABLE CashRegisterWorkingShift_temp1 (
    _id                         INTEGER  NOT NULL
                                        PRIMARY KEY AUTOINCREMENT
                                        UNIQUE,
    EventId                     INTEGER  NOT NULL,
    CashRegisterEventId         INTEGER  NOT NULL,
    ShiftId                     TEXT     NOT NULL,
    MonthEventId                INTEGER  NOT NULL,
    ShiftStartDateTime          DATETIME NOT NULL,
    ShiftEndDateTime            DATETIME,
    OperationDateTime           DATETIME NOT NULL,
    Status                      INTEGER  NOT NULL,
    Number                      INTEGER  NOT NULL,
    PaperConsumption            INTEGER  NOT NULL,
    IsPaperCounterRestarted     BOOLEAN  NOT NULL,
    CashInFR                    TEXT,
    FOREIGN KEY (
        CashRegisterEventId
    )
    REFERENCES CashRegisterEvent (_id),
    FOREIGN KEY (
        MonthEventId
    )
    REFERENCES MonthEvent (_id),
    FOREIGN KEY (
        EventId
    )
    REFERENCES Event (_id)
);

-- Копируем данные о сменах из старой таблицы, вычисляя на лету PaperConsumption
INSERT INTO CashRegisterWorkingShift_temp1
SELECT _id,
       EventId,
       CashRegisterEventId,
       ShiftId,
       MonthEventId,
       ShiftStartDateTime,
       ShiftEndDateTime,
       OperationDateTime,
       Status, Number, max(0, CASE
                                  WHEN Status = 10 THEN PrinterOdometerValue -
                                         (SELECT T.PrinterOdometerValue
                                          FROM CashRegisterWorkingShift AS T
                                          WHERE Status = 0
                                            AND T.ShiftId = CashRegisterWorkingShift.ShiftId)
                                  ELSE 0
                              END),
                       CASE
                           WHEN Status = 10
                                AND PrinterOdometerValue -
                                  (SELECT T.PrinterOdometerValue
                                   FROM CashRegisterWorkingShift AS T
                                   WHERE Status = 0
                                     AND T.ShiftId = CashRegisterWorkingShift.ShiftId) < 0 THEN 1
                           ELSE 0
                       END,
                       CashInFR
FROM CashRegisterWorkingShift;

-- Удаляем старую таблицу CashRegisterWorkingShift
DROP TABLE CashRegisterWorkingShift;
-- Переименовывываем новую таблицу в CashRegisterWorkingShift
ALTER TABLE CashRegisterWorkingShift_temp1 RENAME TO CashRegisterWorkingShift;

-- Создаем новый вариант таблицы TicketTapeEvent:
-- Удалена колонка PrinterOdometerValue
-- Добавлены колонки PaperConsumption и IsPaperCounterRestarted
CREATE TABLE TicketTapeEvent_temp1 (
    _id                        INTEGER  NOT NULL
                                        PRIMARY KEY
                                        UNIQUE,
    EventId                    INTEGER  NOT NULL,
    CashRegisterEventId        INTEGER  NOT NULL,
    CashRegisterWorkingShiftId INTEGER,
    MonthEventId               INTEGER  NOT NULL,
    TicketTapeId               TEXT     NOT NULL,
    Series                     TEXT,
    Number                     TEXT,
    StartTime                  DATETIME NOT NULL,
    EndTime                    DATETIME,
    PaperConsumption           INTEGER  NOT NULL,
    IsPaperCounterRestarted    BOOLEAN  NOT NULL,
    ExpectedFirstDocNumber     INTEGER  NOT NULL
                                        DEFAULT 0,
    FOREIGN KEY (
        CashRegisterEventId
    )
    REFERENCES CashRegisterEvent (_id),
    FOREIGN KEY (
        EventId
    )
    REFERENCES Event (_id),
    FOREIGN KEY (
        MonthEventId
    )
    REFERENCES MonthEvent (_id),
    FOREIGN KEY (
        CashRegisterWorkingShiftId
    )
    REFERENCES CashRegisterWorkingShift (_id)
);

-- Копируем данные о билетной ленте из старой таблицы, вычисляя на лету PaperConsumption
INSERT INTO TicketTapeEvent_temp1
SELECT _id,
       EventId,
       CashRegisterEventId,
       CashRegisterWorkingShiftId,
       MonthEventId,
       TicketTapeId,
       Series, Number, StartTime,
                       EndTime,
                       max(0, CASE
                                  WHEN EndTime IS NOT NULL THEN PrinterOdometerValue -
                                         (SELECT T.PrinterOdometerValue
                                          FROM TicketTapeEvent AS T
                                          WHERE EndTime IS NULL
                                            AND T.TicketTapeId = TicketTapeEvent.TicketTapeId)
                                  ELSE 0
                              END),
                       CASE
                           WHEN EndTime IS NOT NULL
                                AND PrinterOdometerValue -
                                  (SELECT T.PrinterOdometerValue
                                   FROM TicketTapeEvent AS T
                                   WHERE EndTime IS NULL
                                     AND T.TicketTapeId = TicketTapeEvent.TicketTapeId) < 0 THEN 1
                           ELSE 0
                       END,
                       ExpectedFirstDocNumber
FROM TicketTapeEvent;

-- Удаляем старую таблицу TicketTapeEvent
DROP TABLE TicketTapeEvent;
-- Переименовывываем новую таблицу в TicketTapeEvent
ALTER TABLE TicketTapeEvent_temp1 RENAME TO TicketTapeEvent;