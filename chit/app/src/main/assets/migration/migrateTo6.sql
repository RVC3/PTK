ALTER TABLE Event ADD COLUMN deletedMark INTEGER NOT NULL DEFAULT 0;

ALTER TABLE TrainInfo ADD COLUMN deletedMark INTEGER NOT NULL DEFAULT 0;
UPDATE TrainInfo SET deletedMark = 1 where DepartureDate IS NULL;

ALTER TABLE AuthInfo ADD COLUMN AuthorizationDate DATETIME;
UPDATE AuthInfo SET AuthorizationDate = date();


