CREATE TABLE AdditionalInfoForEtt1 (
    _id                   INTEGER  NOT NULL
                                   PRIMARY KEY AUTOINCREMENT
                                   UNIQUE,
    PassengerCategory     TEXT,
    IssueDataTime         DATETIME,
    IssueUnitCode         TEXT,
    OwnerOrganizationCode TEXT,
    PassengerFio          TEXT,
    GuardianFio           TEXT,
    SNILS                 TEXT
);

INSERT INTO AdditionalInfoForEtt1 (_id, PassengerCategory, IssueDataTime, IssueUnitCode, OwnerOrganizationCode, PassengerFio, GuardianFio, SNILS)
select _id, PassengerCategory, IssueDataTime, IssueUnitCode, OwnerOrganizationCode, PassengerFio, GuardianFio, SNILS
from AdditionalInfoForEtt;

UPDATE AdditionalInfoForEtt1 SET IssueDataTime = null WHERE IssueDataTime<10;

UPDATE AdditionalInfoForEtt1 SET PassengerCategory = 'Р' WHERE PassengerCategory = 'r';

DROP TABLE AdditionalInfoForEtt;

ALTER TABLE AdditionalInfoForEtt1 RENAME TO AdditionalInfoForEtt;