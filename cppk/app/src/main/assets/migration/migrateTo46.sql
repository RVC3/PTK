DROP TABLE IF EXISTS Cashier;
CREATE TABLE Cashier (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		UserLogin 							TEXT NOT NULL,
		OfficialCode 						TEXT NOT NULL, 
		Fio								TEXT NOT NULL
);

DROP TABLE IF EXISTS CashRegister;
CREATE TABLE CashRegister (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		Model 								TEXT,
		SerialNumber						TEXT,
		INN 								TEXT,
		EKLZNumber 							TEXT
);

DROP TABLE IF EXISTS StationDevice;
CREATE TABLE StationDevice (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		DeviceId 							INTEGER NOT NULL,
		Model 								TEXT NOT NULL,
		SerialNumber 						TEXT NOT NULL,
		Type 								INTEGER NOT NULL,
		ProductionSectionCode				INTEGER
);

DROP TABLE IF EXISTS Event;
CREATE TABLE Event (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		Guid 								TEXT NOT NULL,
		CreationTimestamp 					DATETIME NOT NULL,
		VersionId 							INTEGER NOT NULL,
		SoftwareUpdateEventId 				INTEGER NOT NULL,
		StationDeviceId						INTEGER NOT NULL,
		StationCode							INTEGER,
		
		FOREIGN KEY(StationDeviceId) REFERENCES StationDevice(_id),
		FOREIGN KEY(SoftwareUpdateEventId) REFERENCES UpdateEvent(_id)
);

DROP TABLE IF EXISTS AdditionalInfoForEtt;
CREATE TABLE AdditionalInfoForEtt (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		PassengerCategory 					TEXT,
		IssueDataTime 						DATETIME NOT NULL,
		IssueUnitCode 						TEXT,
		OwnerOrganizationCode 				TEXT,
		PassengerFio 						TEXT,
		GuardianFio 						TEXT,
		SNILS 								TEXT
);

DROP TABLE IF EXISTS ParentTicketInfo;
CREATE TABLE ParentTicketInfo (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		SaleDateTime 						DATETIME NOT NULL,
		TicketNumber 						INTEGER NOT NULL,
		CashRegisterNumber 					TEXT NOT NULL, 
		WayType 							INTEGER NOT NULL
);

DROP TABLE IF EXISTS SmartCard;
CREATE TABLE SmartCard (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		OuterNumber 						TEXT NOT NULL,
		CrystalSerialNumber 				TEXT NOT NULL,
		TypeCode 							INTEGER NOT NULL,
		Issuerr 							TEXT,
		UsageCount 							INTEGER,
		Track 								INTEGER,
		PresentTicket1 						INTEGER,
		PresentTicket2 						INTEGER,

		FOREIGN KEY(PresentTicket1) REFERENCES ParentTicketInfo(_id),
		FOREIGN KEY(PresentTicket2) REFERENCES ParentTicketInfo(_id)
);

DROP TABLE IF EXISTS Exemption;
CREATE TABLE Exemption (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		Fio 								TEXT,
		Code 								INTEGER NOT NULL,
		ActiveFromDate 						INTEGER NOT NULL,
		VersionId 							INTEGER NOT NULL,
		ExpressCode 						INTEGER NOT NULL,
		LossSum 							TEXT NOT NULL,
		SmartCardId		 					INTEGER,
		TypeOfDocument 						TEXT,
		NumberOfDocument 					TEXT,
		Organization 						TEXT,
		RegionOkatoCode 					TEXT,
		RequireSocialCard 					BOOLEAN NOT NULL DEFAULT '0',
		IsSnilsUsed							BOOLEAN NOT NULL DEFAULT '0',
		
		FOREIGN KEY(SmartCardId) REFERENCES SmartCard(_id)
);

DROP TABLE IF EXISTS CheckTable;
CREATE TABLE CheckTable (
		_id							INTEGER NOT NULL
										PRIMARY KEY AUTOINCREMENT
										UNIQUE,
		SpndNumber 						INTEGER NOT NULL, 
		AdditionalInfo 					TEXT,
		PrintDateTime 					DATETIME NOT NULL,
		SerialNumber 					INTEGER NOT NULL 
);

DROP TABLE IF EXISTS SeasonTicket;
CREATE TABLE SeasonTicket (
		_id 							INTEGER NOT NULL
										PRIMARY KEY AUTOINCREMENT
										UNIQUE,
		PassCount 						INTEGER NOT NULL,
		PassLeftCount 					INTEGER NOT NULL,
		MonthDays 						TEXT
);

DROP TABLE IF EXISTS TrainInfo;
CREATE TABLE TrainInfo (
		_id 							INTEGER NOT NULL
										PRIMARY KEY AUTOINCREMENT
										UNIQUE,
		TrainCategory 					TEXT NOT NULL,
		CarClass 						TEXT,
		TrainCategoryCode 				INTEGER NOT NULL
);

DROP TABLE IF EXISTS Price;
CREATE TABLE Price (
		_id 							INTEGER NOT NULL
										PRIMARY KEY AUTOINCREMENT
										UNIQUE,
		Full 							TEXT NOT NULL,
		Nds 							TEXT NOT NULL,
		Payed 							TEXT NOT NULL,
		SummForReturn 					TEXT NOT NULL
);

DROP TABLE IF EXISTS Fee;
CREATE TABLE Fee (
		_id 							INTEGER NOT NULL
										PRIMARY KEY AUTOINCREMENT
										UNIQUE,
		Total 							TEXT NOT NULL,
		Nds 							TEXT NOT NULL
);

DROP TABLE IF EXISTS LegalEntity;
DROP TABLE IF EXISTS LeganEntity;
CREATE TABLE LegalEntity (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		Code 								TEXT NOT NULL,
		INN 								TEXT NOT NULL,
		Name 								TEXT NOT NULL
);

DROP TABLE IF EXISTS CashRegisterEvent;
CREATE TABLE CashRegisterEvent (
		_id 							INTEGER NOT NULL
										PRIMARY KEY AUTOINCREMENT
										UNIQUE,
		CashRegisterId 					INTEGER NOT NULL,
		CashierId 						INTEGER NOT NULL,
		
		
		FOREIGN KEY(CashierId) REFERENCES Cashier(_id),
		FOREIGN KEY(CashRegisterId) REFERENCES CashRegister(_id)
);

DROP TABLE IF EXISTS Months;
DROP TABLE IF EXISTS MonthEvent;
CREATE TABLE MonthEvent (
		_id 						INTEGER NOT NULL
									PRIMARY KEY AUTOINCREMENT
									UNIQUE,
		EventId 					INTEGER NOT NULL,
		MonthId						TEXT NOT NULL,
		CashRegisterEventId 		INTEGER NOT NULL,
		Status				 		INTEGER NOT NULL,
		StartTimestamp 				DATETIME NOT NULL,
		EndTimestamp 				DATETIME,
		Number 						INTEGER NOT NULL,
		
		FOREIGN KEY(EventId) REFERENCES Event(_id),
		FOREIGN KEY(CashRegisterEventId) REFERENCES CashRegisterEvent(_id)
);

DROP TABLE IF EXISTS CashRegisterWorkingShift;
DROP TABLE IF EXISTS CashRegisterWorkingShifts;
CREATE TABLE CashRegisterWorkingShift (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		EventId 							INTEGER NOT NULL,
		CashRegisterEventId 				INTEGER NOT NULL,
		ShiftId								TEXT NOT NULL,
		MonthEventId 						INTEGER NOT NULL,
		ShiftStartDateTime					DATETIME NOT NULL,
		ShiftEndDateTime					DATETIME,
		OperationDateTime					DATETIME NOT NULL,
		Status								INTEGER NOT NULL,
		Number								INTEGER NOT NULL,
		PrinterOdometerValue 		        INTEGER NOT NULL,
		CashInFR 							TEXT,
		
		FOREIGN KEY(CashRegisterEventId) REFERENCES CashRegisterEvent(_id),
		FOREIGN KEY(MonthEventId) REFERENCES Months(_id),
		FOREIGN KEY(EventId) REFERENCES Event(_id)
);

DROP TABLE IF EXISTS TerminalDay;
CREATE TABLE TerminalDay (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		TerminalDayId 						INTEGER NOT NULL,
		EventId 							INTEGER NOT NULL,
		StartCashRegisterWorkingShiftId 	INTEGER NOT NULL,
		EndCashRegisterWorkingShiftId 	    INTEGER,
		StartDateTime 	                    INTEGER NOT NULL,
		EndDateTime 	                    INTEGER,
		Report				        		TEXT,

        FOREIGN KEY(EventId) REFERENCES Event(_id),
        FOREIGN KEY(StartCashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id),
        FOREIGN KEY(EndCashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id)
);

DROP TABLE IF EXISTS TestTickets;
DROP TABLE IF EXISTS TestTicketEvent;
CREATE TABLE TestTicketEvent (
		_id 							INTEGER NOT NULL
										PRIMARY KEY AUTOINCREMENT
										UNIQUE,
		EventId							INTEGER NOT NULL,
		CashRegisterWorkingShiftId 		INTEGER NOT NULL,
		CheckId							INTEGER NOT NULL,
        TicketTapeEventId 				INTEGER NOT NULL,
		
		FOREIGN KEY(CashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id),
		FOREIGN KEY(CheckId) REFERENCES CheckTable(_id),
		FOREIGN KEY(EventId) REFERENCES Event(_id)
);

DROP TABLE IF EXISTS TicketEventBase;
CREATE TABLE TicketEventBase (
		_id 							INTEGER NOT NULL
										PRIMARY KEY AUTOINCREMENT
										UNIQUE,
		CashRegisterWorkingShiftId 		INTEGER NOT NULL,
		SaleDateTime					DATETIME,
		ValidFromDateTime				DATETIME,
		ValidTillDateTime				DATETIME,
		DepartureStationCode			INTEGER NOT NULL,
		DestinationStationCode			INTEGER NOT NULL,
		TariffCode						INTEGER NOT NULL,
		WayType							INTEGER NOT NULL,
		Type							TEXT NOT NULL,
		TypeCode						INTEGER NOT NULL,
		SmartCardId						INTEGER,
		TicketCategoryCode				INTEGER NOT NULL,
		TicketTypeShortName				TEXT NOT NULL,
		
		FOREIGN KEY(CashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id),
		FOREIGN KEY(SmartCardId) REFERENCES SmartCard(_id)
);

DROP TABLE IF EXISTS CPPKTicketControl;
DROP TABLE IF EXISTS CPPKTicketControls;
CREATE TABLE CPPKTicketControl (
		_id 							INTEGER NOT NULL
										PRIMARY KEY AUTOINCREMENT
										UNIQUE,
		EventId							INTEGER NOT NULL,
		TicketEventBaseId 				INTEGER NOT NULL,
		TicketNumber					INTEGER NOT NULL,
		ControlDateTime					DATETIME NOT NULL,
		EdsKeyNumber					INTEGER NOT NULL,
		IsRevokedEds					BOOLEAN NOT NULL DEFAULT '0',
		StopListId						INTEGER,
		ExemptionCode					INTEGER,
		ParentTicketId					INTEGER,
		ValidationResult				INTEGER NOT NULL,
		TripsSpend						INTEGER,
		SellTicketDeviceId				INTEGER NOT NULL,
		
		FOREIGN KEY(TicketEventBaseId) REFERENCES TicketEventBase(_id),
		FOREIGN KEY(EventId) REFERENCES Event(_id),
		FOREIGN KEY(ParentTicketId) REFERENCES ParentTicketInfo(_id)
);

DROP TABLE IF EXISTS BankTransactionCashRegisterEvent;
CREATE TABLE BankTransactionCashRegisterEvent (
		_id 							INTEGER NOT NULL
										PRIMARY KEY AUTOINCREMENT
										UNIQUE,
        TransactionId					INTEGER NOT NULL,
		EventId							INTEGER NOT NULL,
		CashRegisterWorkingShiftId 		INTEGER NOT NULL,
		TerminalDayId 		            INTEGER NOT NULL,
		MonthId 		            	INTEGER NOT NULL,
		PointOfSaleNumber				TEXT,
		MerchantId						TEXT NOT NULL,
		BankCode						INTEGER,
		OperationType					INTEGER NOT NULL,
		OperationResult					INTEGER NOT NULL,
		Rrn								TEXT NOT NULL,
		AuthorizationCode				TEXT NOT NULL,
		SmartCardApplicationName		TEXT NOT NULL,
		CardPan							TEXT NOT NULL,
		CardEmitentName					TEXT NOT NULL,
		BankCheckNumber					INTEGER NOT NULL,
		TransactionDateTime				DATETIME NOT NULL,
		Total							REAL NOT NULL,
		CurrencyCode					TEXT NOT NULL,
		TerminalNumber					TEXT NOT NULL,
		Status							INTEGER NOT NULL,

		FOREIGN KEY(CashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id),
		FOREIGN KEY(EventId) REFERENCES Event(_id),
		FOREIGN KEY(TerminalDayId) REFERENCES TerminalDay(_id),
		FOREIGN KEY(MonthId) REFERENCES Months(_id)
);

DROP TABLE IF EXISTS TicketSaleReturnEventBase;
CREATE TABLE TicketSaleReturnEventBase (
		_id 									INTEGER NOT NULL
												PRIMARY KEY AUTOINCREMENT
												UNIQUE,
		TicketEventBaseId 						INTEGER NOT NULL,
		BankTransactionCashRegisterEventId		INTEGER,
		IsTicketWritten 						BOOLEAN NOT NULL DEFAULT '0',
		AdditionInfoForEttId 					INTEGER,
		ParentTicketId 							INTEGER,
		ExemptionId 							INTEGER,
		CheckId 								INTEGER,
		Kind 									INTEGER NOT NULL,
		TrainInfoId 							INTEGER NOT NULL,
		SeasonTicketId 							INTEGER,
		IsOneTimeTicket							BOOLEAN NOT NULL DEFAULT '1',
		PriceId 								INTEGER NOT NULL,
		FeeId 									INTEGER,
		PaymentTypeCode 						INTEGER NOT NULL,
		LegalEntityId 							INTEGER NOT NULL,
		
		FOREIGN KEY(TicketEventBaseId) REFERENCES TicketEventBase(_id),
		FOREIGN KEY(BankTransactionCashRegisterEventId) REFERENCES BankTransactionCashRegisterEvent(_id),
		FOREIGN KEY(AdditionInfoForEttId) REFERENCES AdditionalInfoForEtt(_id),
		FOREIGN KEY(ParentTicketId) REFERENCES ParentTicketInfo(_id),
		FOREIGN KEY(ExemptionId) REFERENCES Exemption(_id),
		FOREIGN KEY(CheckId) REFERENCES CheckTable(_id),
		FOREIGN KEY(TrainInfoId) REFERENCES TrainInfo(_id),
		FOREIGN KEY(SeasonTicketId) REFERENCES SeasonTicket(_id),
		FOREIGN KEY(PriceId) REFERENCES Price(_id),
		FOREIGN KEY(FeeId) REFERENCES Fee(_id),
		FOREIGN KEY(LegalEntityId) REFERENCES LegalEntity(_id)
);

DROP TABLE IF EXISTS CPPKTicketSales;
DROP TABLE IF EXISTS TicketSales;
CREATE TABLE CPPKTicketSales (
		_id 							INTEGER NOT NULL
										PRIMARY KEY AUTOINCREMENT
										UNIQUE,
		EventId 						INTEGER NOT NULL,
		TicketSaleReturnEventBaseId 	INTEGER NOT NULL,
		TripsCount						INTEGER,
		StorageTypeCode 				INTEGER NOT NULL,
		EDSKeyNumber 					INTEGER,
        TicketTapeEventId 				INTEGER NOT NULL,
        ProgressStatus 				    INTEGER NOT NULL,
        WriteErrorCode 				    INTEGER,
		FullTicketPrice  				TEXT NOT NULL,

		FOREIGN KEY(TicketSaleReturnEventBaseId) REFERENCES TicketSaleReturnEventBase(_id),
		FOREIGN KEY(EventId) REFERENCES Event(_id)
);

DROP TABLE IF EXISTS CPPKTicketReturn;
DROP TABLE IF EXISTS Recall;
CREATE TABLE CPPKTicketReturn (
		_id 										INTEGER NOT NULL
													PRIMARY KEY AUTOINCREMENT
													UNIQUE,
		EventId 									INTEGER NOT NULL,
		CashRegisterWorkingShiftId					INTEGER NOT NULL,
		CppkTicketSaleEventId						INTEGER NOT NULL,
		ReturnBankTransactionCashRegisterEventId	INTEGER,
		PriceId 									INTEGER NOT NULL,
		ReturnCheckId 								INTEGER,
		RecallDateTime 								DATETIME,
		RecallReason 								TEXT NOT NULL,
		ReturmPaymentTypeCode 						INTEGER NOT NULL,
		ReturnOperationTypeCode 					INTEGER NOT NULL,
        TicketTapeEventId 							INTEGER NOT NULL,
        SumToReturn 	 							TEXT NOT NULL,
        ProgressStatus 	 							INTEGER NOT NULL,
        ReturnBankTerminalSlip	                    TEXT,

		FOREIGN KEY(CppkTicketSaleEventId) REFERENCES CPPKTicketSales(_id),
		FOREIGN KEY(ReturnBankTransactionCashRegisterEventId) REFERENCES BankTransactionCashRegisterEvent(_id),
		FOREIGN KEY(EventId) REFERENCES Event(_id),
		FOREIGN KEY(CashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id),
		FOREIGN KEY(PriceId) REFERENCES Price(_id),
		FOREIGN KEY(ReturnCheckId) REFERENCES CheckTable(_id)
);

DROP TABLE IF EXISTS PrintReports;
DROP TABLE IF EXISTS PrintReportEvent;
CREATE TABLE PrintReportEvent (
		_id 								INTEGER NOT NULL
											PRIMARY KEY
											UNIQUE,
		CashRegisterEventId 		        INTEGER NOT NULL,
        CashRegisterWorkingShiftId  		INTEGER,
        MonthEventId  						INTEGER NOT NULL,
		EventId 							INTEGER NOT NULL,
		ReportType 							INTEGER,
		TicketTapeEventId 					INTEGER NOT NULL,
		OperationTime						DATETIME NOT NULL,
		CashInFR 							TEXT NOT NULL,

        FOREIGN KEY(CashRegisterEventId) REFERENCES CashRegisterEvent(_id),
        FOREIGN KEY(EventId) REFERENCES Event(_id),
        FOREIGN KEY(MonthEventId) REFERENCES Months(_id),
        FOREIGN KEY(CashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id)
);

DROP TABLE IF EXISTS AuditTrailEvent;
CREATE TABLE AuditTrailEvent (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
		Type 								INTEGER NOT NULL,
		ExtEventId 							INTEGER NOT NULL,
		OperationTime						DATETIME NOT NULL,
		CashRegisterWorkingShiftId  		INTEGER,
		MonthEventId  						INTEGER NOT NULL,

        FOREIGN KEY(MonthEventId) REFERENCES Months(_id),
        FOREIGN KEY(CashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id)
);

DROP TABLE IF EXISTS TicketTape;
DROP TABLE IF EXISTS TicketTapeEvent;
CREATE TABLE TicketTapeEvent (
		_id 								INTEGER NOT NULL
											PRIMARY KEY
											UNIQUE,
		EventId 							INTEGER NOT NULL,
		CashRegisterEventId 		        INTEGER NOT NULL,
		CashRegisterWorkingShiftId  		INTEGER,
		MonthEventId  						INTEGER NOT NULL,
		TicketTapeId						TEXT NOT NULL,
		Series 								TEXT,
		Number 								TEXT,
        StartTime 							DATETIME NOT NULL,
        EndTime 							DATETIME,
		PrinterOdometerValue 		        INTEGER NOT NULL,

        FOREIGN KEY(CashRegisterEventId) REFERENCES CashRegisterEvent(_id),
        FOREIGN KEY(EventId) REFERENCES Event(_id),
        FOREIGN KEY(MonthEventId) REFERENCES Months(_id),
        FOREIGN KEY(CashRegisterWorkingShiftId) REFERENCES CashRegisterWorkingShift(_id)
);

DROP TABLE IF EXISTS UpdateHistory;
DROP TABLE IF EXISTS UpdateEvent;
CREATE TABLE UpdateEvent (
		_id 								INTEGER NOT NULL
											PRIMARY KEY AUTOINCREMENT
											UNIQUE,
        OperationTime 						DATETIME NOT NULL,
		Version								TEXT NOT NULL,
		UpdateSubject 						INTEGER NOT NULL
);

DROP TABLE IF EXISTS PtkSettingsCommon;
CREATE TABLE PtkSettingsCommon (
		ppReportOpenShift 							VARCHAR,
		ppReportCloseShift 							VARCHAR,
		ppReportCloseMonth 							VARCHAR,
		pTestPdPrintReq			 					BOOLEAN NOT NULL DEFAULT(1),
		pDiscountShiftSheetOpeningShift 			BOOLEAN NOT NULL DEFAULT(0),
		pDiscountShiftSheetClosingShiftReq 			BOOLEAN NOT NULL DEFAULT(1),
		pSheetShiftCloseShiftReq 					BOOLEAN NOT NULL DEFAULT(1),
		pSheetBlankingShiftClosingShiftReq 			BOOLEAN NOT NULL DEFAULT(1),
		pDiscountMonthShiftSheetClosingMonthReq 	BOOLEAN NOT NULL DEFAULT(1),
		pMonthSheetClosingMonthReq 					BOOLEAN NOT NULL DEFAULT(1),
		pSheetBlankingMonthClosingMonthReq 			BOOLEAN NOT NULL DEFAULT(1),
		pBTMonthlySheetClosingMonthReq 				BOOLEAN NOT NULL DEFAULT(1),
		timeChangesPeriod 							INT NOT NULL DEFAULT(5),
		enableAnnulateAfterTimeOver 				BOOLEAN NOT NULL DEFAULT(1),
		termStoragePd 								INT NOT NULL DEFAULT(13),
		durationOfPdNextDay 						INTEGER NOT NULL DEFAULT(2),
		maxTimeAgoMark 								INT NOT NULL DEFAULT(4),
		timeElectronicRegistration 					INT NOT NULL DEFAULT(1),
		bankCode 									INT NOT NULL DEFAULT(1)
);