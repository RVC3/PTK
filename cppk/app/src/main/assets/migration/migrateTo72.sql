CREATE TABLE PtkSettingsPrivateTemp  (
    name  TEXT PRIMARY KEY
               UNIQUE
               NOT NULL
               DEFAULT UNKNOWN,
    value TEXT
);

INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("TerminalNumber", (select terminalNumber as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("TrainCategoryPrefix", (select trainCategoryPrefix as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("ProductionSectionCode", (select productionSectionId as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("IsAutoTimeSyncEnabled", (select
                                                                                       CASE
                                                                                           WHEN isAutoTimeSyncEnabled = 1 THEN 'true'
                                                                                           WHEN isAutoTimeSyncEnabled = 0 THEN 'false'
                                                                                           ELSE isAutoTimeSyncEnabled
                                                                                       END
                                                                                            as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("TimeToCloseShiftMessage", (select timeToCloseShiftMessage as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("StopListValidTime", (select stopListValidTime as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("WorkStationCode", (select currentStationCode as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("IsPosEnabled", (select
                                                                              CASE
                                                                                  WHEN isPosEnabled = 1 THEN 'true'
                                                                                  WHEN isPosEnabled = 0 THEN 'false'
                                                                                  ELSE isPosEnabled
                                                                              END
                                                                                  as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("TimeForAnnulate", (select timeForAnnulate as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("IsOutputMode", (select
                                                                              CASE
                                                                                  WHEN isOutputMode = 1 THEN 'true'
                                                                                  WHEN isOutputMode = 0 THEN 'false'
                                                                                  ELSE isOutputMode
                                                                              END
                                                                                  as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("IsMobileCashRegister", (select
                                                                                      CASE
                                                                                          WHEN isMobileCashRegister = 1 THEN 'true'
                                                                                          WHEN isMobileCashRegister = 0 THEN 'false'
                                                                                          ELSE isMobileCashRegister
                                                                                      END
                                                                                          as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("IsSaleEnabled", (select
                                                                               CASE
                                                                                   WHEN isSaleEnabled = 1 THEN 'true'
                                                                                   WHEN isSaleEnabled = 0 THEN 'false'
                                                                                   ELSE isSaleEnabled
                                                                               END
                                                                                   as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("IsTimeSyncEnabled", (select
                                                                                   CASE
                                                                                       WHEN isTimeSyncEnabled = 1 THEN 'true'
                                                                                       WHEN isTimeSyncEnabled = 0 THEN 'false'
                                                                                       ELSE isTimeSyncEnabled
                                                                                   END
                                                                                       as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("DayCode", (select dayCode as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));
INSERT INTO PtkSettingsPrivateTemp (name, value) VALUES ("IsUseMobileData", (select
                                                                                 CASE
                                                                                     WHEN isUseMobileData = 1 THEN 'true'
                                                                                     WHEN isUseMobileData = 0 THEN 'false'
                                                                                     ELSE isUseMobileData
                                                                                 END
                                                                                     as T FROM PtkSettingsPrivate WHERE T IS NOT NULL ORDER BY isDefault ASC LIMIT 1));

DROP TABLE IF EXISTS PtkSettingsPrivate;

ALTER TABLE PtkSettingsPrivateTemp RENAME TO PtkSettingsPrivate;