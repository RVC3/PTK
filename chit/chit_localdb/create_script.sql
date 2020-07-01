CREATE TABLE "LocalDbVersion" (
    _id                   			INTEGER PRIMARY KEY,
	UpgradeDateTime	     			INTEGER,
    Description           	        TEXT
);

INSERT INTO "LocalDbVersion" VALUES (1, strftime('%s','now') * 1000, "Первая версия");

CREATE TABLE "AuthInfo" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
    BaseUri     					TEXT,
    AuthorizationCode               TEXT,
    ClientId           				TEXT,
    ClientSecret 					TEXT,
    TerminalId          			INTEGER,
    BaseStationId          			TEXT,
    Thumbprint           			TEXT,
    SerialNumber                 	TEXT
);

CREATE TABLE "OAuth2Token" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
    AccessToken     				TEXT,
    TokenType           			TEXT,
    RefreshToken           		    TEXT,
    Issued 							TEXT,
    Expires          				TEXT,
    ExpiresIn           			INTEGER,
    ClientId                 		TEXT,
	Broken						    INTEGER,
	AuthInfoId					    INTEGER,
	FOREIGN KEY (AuthInfoId) REFERENCES AuthInfo(_id)
);

CREATE TABLE "User" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
    Name     						TEXT
);

CREATE TABLE "ControlStation" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
    Code		           			INTEGER,
    DepartureDate		           	DATETIME
);

CREATE TABLE "TrainInfo" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	TrainThreadId     		        TEXT,
	TrainNumber     				TEXT,
    DepartureStationCode		    INTEGER,
	DestinationStationCode	        INTEGER,
	DepartureDate		            DATETIME,
	DestinationDate		            DATETIME,
    Legacy                          INTEGER
);

CREATE TABLE "TripServiceEvent" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
    TripUuid     					TEXT,
	EventId	     					INTEGER,
    Status           				INTEGER,
    StartTime           			DATETIME,
    EndTime          				DATETIME,
    UserId           				INTEGER,
    TrainInfoId           			INTEGER,
	FOREIGN KEY (UserId) REFERENCES User(_id),
	FOREIGN KEY (EventId) REFERENCES Event(_id),
	FOREIGN KEY (TrainInfoId) REFERENCES TrainInfo(_id)
);

CREATE TABLE "StationInfo" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	TrainInfoId     				INTEGER,
	Code     						INTEGER,
    Number							INTEGER,
	StationStateCode			    INTEGER,
	ArrivalDate		        		DATETIME,
	DepartureDate		            DATETIME,
	FOREIGN KEY (TrainInfoId) REFERENCES TrainInfo(_id)
);

CREATE TABLE "CarInfo" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	TrainInfoId     				INTEGER,
	Number     					    TEXT,
    SchemeId						INTEGER,
	FOREIGN KEY (SchemeId) REFERENCES CarScheme(_id),
	FOREIGN KEY (TrainInfoId) REFERENCES TrainInfo(_id)
);

CREATE TABLE "CarScheme" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	Height     						INTEGER,
	Width     						INTEGER
);

CREATE TABLE "CarSchemeElement" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	CarSchemeId     				INTEGER,
	CarSchemeElementKind		    INTEGER,
	X     							INTEGER,
	Y     							INTEGER,
	Height     						INTEGER,
	Width     						INTEGER,
	PlaceNumber     				TEXT,
	PlaceDirection		    		INTEGER,
	FOREIGN KEY (CarSchemeId) REFERENCES CarScheme(_id)
);

CREATE TABLE "TicketId" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	TicketNumber     			    INTEGER NOT NULL,
	SaleDate     					DATETIME NOT NULL,
	DeviceId     					TEXT NOT NULL,
	UNIQUE (TicketNumber, SaleDate, DeviceId)
);

CREATE TABLE "PassengerPersonalData" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	DocumentTypeCode     	        INTEGER,
	DocumentNumber     		        TEXT,
	LastName     					TEXT,
	FirstName     					TEXT,
	MiddleName     				    TEXT,
	Gender     						INTEGER,
	Birthday     					TEXT
);

CREATE TABLE "PlaceLocation" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	CarNumber     				    TEXT,
	PlaceNumber     				TEXT
);

CREATE TABLE "Ticket" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	TicketIdId     					INTEGER,
	TrainThreadCode			        TEXT,
	TrainNumber					    TEXT,
	DepartureStationCode		    INTEGER,
	DestinationStationCode          INTEGER,
	DepartureDate     			    DATETIME,
	TicketTypeCode     		        INTEGER,
	ExemptionCode     			    INTEGER,
	TicketIssueType     		    INTEGER,
	TicketState     				INTEGER,
	StateDate     					DATETIME,
	PassengerId     				INTEGER,
	PlaceLocationId     			INTEGER,
	OldPlaceLocationId     		    INTEGER,
	NsiVersion     					INTEGER,
	FOREIGN KEY (TicketIdId) REFERENCES TicketId(_id),
	FOREIGN KEY (PassengerId) REFERENCES PassengerPersonalData(_id),
	FOREIGN KEY (PlaceLocationId) REFERENCES PlaceLocation(_id),
	FOREIGN KEY (OldPlaceLocationId) REFERENCES PlaceLocation(_id)
);

CREATE TABLE "Event" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	CreatedAt	     				DATETIME
);

CREATE TABLE "Passenger" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	FirstName	     				TEXT,
	LastName	     				TEXT,
	MiddleName	     			    TEXT,
	DocumentTypeCode	            INTEGER,
	DocumentNumber	     	        TEXT
);

CREATE TABLE "Location" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	CarNumber	     				TEXT,
	PlaceNumber	     			    TEXT
);

CREATE TABLE "SmartCard" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	OuterNumber	     			    TEXT,
	CrystalSerialNumber	     	    TEXT,
	Type	     					INTEGER,
	UsageCount	    			    INTEGER
);

CREATE TABLE "TicketData" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	TicketTypeCode	     		    INTEGER,
	PassengerId	     			    INTEGER,
	LocationId	     				INTEGER,
	DepartureDate	     		    DATETIME,
	DepartureStationCode	     	INTEGER,
	DestinationStationCode	     	INTEGER,
	TariffId	     				INTEGER,
	ExemptionCode	     		    INTEGER,
	SmartCardId	     			    INTEGER,
	NsiVersion	     			    INTEGER,
	FOREIGN KEY (PassengerId) REFERENCES Passenger(_id),
	FOREIGN KEY (LocationId) REFERENCES Location(_id),
	FOREIGN KEY (SmartCardId) REFERENCES SmartCard(_id)
);

CREATE TABLE "TicketBoarding" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	TicketIdId	     				INTEGER,
	TrainNumber	     			    TEXT,
	TrainThreadId     		        TEXT,
	TerminalDeviceId	     		TEXT,
	OperatorName	     		    TEXT,
	ControlStationCode	     	    INTEGER,
	EdsKeyNumber	     		    INTEGER,
	EdsValid	     				INTEGER,
	InWhiteList	     			    INTEGER,
	StopListRefusalCode	            INTEGER,
	CheckDate	     				DATETIME,
	WasBoarded	     			    INTEGER,
	TicketDataId	     			INTEGER,
	FOREIGN KEY (TicketIdId) REFERENCES TicketId(_id),
	FOREIGN KEY (TicketDataId) REFERENCES TicketData(_id)
);

CREATE TABLE "BoardingEvent" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
    BoardingUuid     				TEXT,
	EventId	     					INTEGER,
	TripServiceEventId	     	    INTEGER,
    StartTime           			DATETIME,
    EndTime          				DATETIME,
	StationCode	     	            INTEGER,
    Status           				INTEGER,
	FOREIGN KEY (EventId) REFERENCES Event(_id)
	FOREIGN KEY (TripServiceEventId) REFERENCES TripServiceEvent(_id)
);

CREATE TABLE "BoardingExportEvent" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	EventId	     					INTEGER,
    BoardingEventId           	    INTEGER,
	FOREIGN KEY (EventId) REFERENCES Event(_id),
	FOREIGN KEY (BoardingEventId) REFERENCES BoardingEvent(_id)
);

CREATE TABLE "TicketControlEvent" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	EventId	     					INTEGER,
	TicketBoardingId	     		INTEGER,
	Status	     					INTEGER,
	BoardingEventId           	    INTEGER,
	FOREIGN KEY (EventId) REFERENCES Event(_id),
	FOREIGN KEY (TicketBoardingId) REFERENCES TicketBoarding(_id),
	FOREIGN KEY (BoardingEventId) REFERENCES BoardingEvent(_id)
);

CREATE TABLE "TicketControlExportEvent" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	EventId	     					INTEGER,
    TicketControlEventId            INTEGER,
	FOREIGN KEY (EventId) REFERENCES Event(_id),
	FOREIGN KEY (TicketControlEventId) REFERENCES TicketControlEvent(_id)
);

CREATE TABLE "ExchangeEvent" (
    _id                   			INTEGER PRIMARY KEY AUTOINCREMENT,
	EventId	     					INTEGER,
    Type                            INTEGER,
    Status                          INTEGER,
	FOREIGN KEY (EventId) REFERENCES Event(_id)
);

CREATE TABLE "AppProperty" (
    Key                   		    TEXT PRIMARY KEY,
    Value     					    TEXT
);
