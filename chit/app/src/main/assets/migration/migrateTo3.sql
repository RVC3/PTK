CREATE TABLE `TicketTemp` (
    `_id`                    `INTEGER`  PRIMARY KEY AUTOINCREMENT,
    `TicketIdId`             `INTEGER`,
    `TrainThreadCode`        `TEXT`,
    `TrainNumber`            `TEXT`,
    `DepartureStationCode`   `INTEGER`,
    `DestinationStationCode` `INTEGER`,
    `DepartureDate`          `DATETIME`,
    `TicketTypeCode`         `INTEGER`,
    `ExemptionExpressCode`   `INTEGER`,
    `TicketIssueType`        `INTEGER`,
    `TicketState`            `INTEGER`,
    `StateDate`              `DATETIME`,
    `PassengerId`            `INTEGER`,
    `PlaceLocationId`        `INTEGER`,
    `OldPlaceLocationId`     `INTEGER`,
    `NsiVersion`             `INTEGER`,
    FOREIGN KEY (
        `TicketIdId`
    )
    REFERENCES `TicketId` (`_id`),
    FOREIGN KEY (
        `PassengerId`
    )
    REFERENCES `PassengerPersonalData` (`_id`),
    FOREIGN KEY (
        `PlaceLocationId`
    )
    REFERENCES `PlaceLocation` (`_id`),
    FOREIGN KEY (
        `OldPlaceLocationId`
    )
    REFERENCES `PlaceLocation` (`_id`)
);

INSERT INTO TicketTemp SELECT * FROM Ticket;

DROP TABLE Ticket;

ALTER TABLE TicketTemp RENAME TO Ticket;

CREATE TABLE `TicketDataTemp` (
    `_id`                    `INTEGER`  PRIMARY KEY AUTOINCREMENT,
    `TicketTypeCode`         `INTEGER`,
    `PassengerId`            `INTEGER`,
    `LocationId`             `INTEGER`,
    `DepartureDate`          `DATETIME`,
    `DepartureStationCode`   `INTEGER`,
    `DestinationStationCode` `INTEGER`,
    `TariffId`               `INTEGER`,
    `ExemptionExpressCode`   `INTEGER`,
    `SmartCardId`            `INTEGER`,
    `NsiVersion`             `INTEGER`,
    FOREIGN KEY (
        `PassengerId`
    )
    REFERENCES `Passenger` (`_id`),
    FOREIGN KEY (
        `LocationId`
    )
    REFERENCES `Location` (`_id`),
    FOREIGN KEY (
        `SmartCardId`
    )
    REFERENCES `SmartCard` (`_id`)
);

INSERT INTO TicketDataTemp SELECT * FROM TicketData;

DROP TABLE TicketData;

ALTER TABLE TicketDataTemp RENAME TO TicketData;
