CREATE TABLE PtkSettingsPrivate_temp1 (
    terminalNumber          INT,
    productionSectionId     INTEGER,
    isMobileCashRegister    BOOLEAN,
    currentStationCode      INTEGER,
    isOutputMode            BOOLEAN,
    timeForAnnulate         INT,
    trainCategoryPrefix       INT,
    stopListValidTime       INT,
    timeToCloseShiftMessage INT,
    dayCode                 INT,
    isTimeSyncEnabled       BOOLEAN,
    isAutoTimeSyncEnabled   BOOLEAN,
    isDefault               BOOLEAN,
    isPosEnabled            BOOLEAN,
    isSaleEnabled           BOOLEAN,
    isUseMobileData         BOOLEAN
);
INSERT INTO PtkSettingsPrivate_temp1 SELECT * FROM PtkSettingsPrivate;
DROP TABLE PtkSettingsPrivate;
ALTER TABLE PtkSettingsPrivate_temp1 RENAME TO PtkSettingsPrivate;
UPDATE PtkSettingsPrivate SET trainCategoryPrefix = 6000 WHERE trainCategoryPrefix is not null;