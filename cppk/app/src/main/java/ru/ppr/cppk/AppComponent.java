package ru.ppr.cppk;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactory;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.SamAuthorizationStrategyFactory;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.PdEncoderFactory;
import ru.ppr.core.dataCarrier.pd.PdVersionChecker;
import ru.ppr.core.dataCarrier.readbarcodetask.ReadBarcodeTaskFactory;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaListDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoderFactory;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataDecoderFactory;
import ru.ppr.core.helper.Resources;
import ru.ppr.core.helper.Toaster;
import ru.ppr.core.logic.FioFormatter;
import ru.ppr.core.logic.FioNormalizer;
import ru.ppr.core.logic.interactor.DeviceIdChecker;
import ru.ppr.core.manager.BarcodeManager;
import ru.ppr.core.manager.BatteryManager;
import ru.ppr.core.manager.IBluetoothManager;
import ru.ppr.core.manager.RfidManager;
import ru.ppr.core.manager.ScreenManager;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.core.manager.network.AirplaneModeManager;
import ru.ppr.core.ui.mvp.core.MvpProcessor;
import ru.ppr.cppk.Sounds.BeepPlayer;
import ru.ppr.cppk.backup.FullBackupCreator;
import ru.ppr.cppk.backup.FullBackupRestorer;
import ru.ppr.cppk.backup.LocalDbBackupCreator;
import ru.ppr.cppk.backup.LocalDbBackupRestorer;
import ru.ppr.cppk.backup.LogBackupCreator;
import ru.ppr.cppk.backup.NsiBackupCreator;
import ru.ppr.cppk.backup.NsiBackupRestorer;
import ru.ppr.cppk.backup.PrinterDbBackupCreator;
import ru.ppr.cppk.backup.PrinterDbBackupRestorer;
import ru.ppr.cppk.backup.SecurityBackupCreator;
import ru.ppr.cppk.backup.SecurityBackupRestorer;
import ru.ppr.cppk.backup.SftBackupCreator;
import ru.ppr.cppk.backup.SftBackupRestorer;
import ru.ppr.cppk.backup.SyncBackupCreator;
import ru.ppr.cppk.data.summary.RecentStationsStatistics;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.repository.PrivateSettingsRepository;
import ru.ppr.cppk.db.local.repository.ServiceTicketControlEventRepository;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.AppVersionUpdateRegister;
import ru.ppr.cppk.helpers.CommonSettingsStorage;
import ru.ppr.cppk.helpers.CommonSettingsTempStorage;
import ru.ppr.cppk.helpers.DeviceSessionInfo;
import ru.ppr.cppk.helpers.EdsManagerConfigSyncronizer;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.PaperUsageCounter;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.helpers.TicketTapeRestChecker;
import ru.ppr.cppk.helpers.TicketTypeChecker;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.helpers.UserSessionInfo;
import ru.ppr.cppk.helpers.controlbarcodestorage.PdControlBarcodeDataStorage;
import ru.ppr.cppk.helpers.controlbscstorage.PdControlCardDataStorage;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardDataStorage;
import ru.ppr.cppk.localdb.repository.LocalDbVersionRepository;
import ru.ppr.cppk.localdb.repository.UpdateEventRepository;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;
import ru.ppr.cppk.logic.BarcodeBuilder;
import ru.ppr.cppk.logic.CacheUpdater;
import ru.ppr.cppk.logic.CriticalNsiChecker;
import ru.ppr.cppk.logic.DocumentNumberProvider;
import ru.ppr.cppk.logic.FiscalHeaderParamsBuilder;
import ru.ppr.cppk.logic.LogEventBuilder;
import ru.ppr.cppk.logic.NeedCreateControlEventChecker;
import ru.ppr.cppk.logic.PDSignChecker;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.logic.PtkModeChecker;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.TestPdSaleDocumentFactory;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.logic.TransferPdChecker;
import ru.ppr.cppk.logic.TransferSaleButtonDetector;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.cppk.logic.creator.BankTransactionEventCreator;
import ru.ppr.cppk.logic.creator.CashRegisterEventCreator;
import ru.ppr.cppk.logic.creator.MonthEventCreator;
import ru.ppr.cppk.logic.creator.ShiftEventCreator;
import ru.ppr.cppk.logic.creator.TicketTapeEventCreator;
import ru.ppr.cppk.logic.creator.UpdateEventCreator;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSyncChecker;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSynchronizer;
import ru.ppr.cppk.logic.fiscaldocument.TestPdSaleDocumentStateSyncronizer;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.logic.interactor.PrintTestCheckInteractor;
import ru.ppr.cppk.logic.interactor.TicketTypeValidityPeriodCalculator;
import ru.ppr.cppk.logic.pd.PdHandler;
import ru.ppr.cppk.logic.pd.checker.TransferRouteChecker;
import ru.ppr.cppk.logic.pd.checker.ValidAndControlNeededChecker;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.managers.FileCleaner;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.pd.check.control.StrategyCheckFactory;
import ru.ppr.cppk.pd.check.write.OneOffAndSeasonForPeriodChecker;
import ru.ppr.cppk.pd.check.write.SeasonForDaysTicketForWriteChecker;
import ru.ppr.cppk.pd.utils.TicketControlEventCreator;
import ru.ppr.cppk.pos.PosTerminalFactory;
import ru.ppr.cppk.printer.rx.operation.base.OperationFactory;
import ru.ppr.cppk.settings.CommonMenuViewManager;
import ru.ppr.cppk.settings.SplashViewManager;
import ru.ppr.cppk.ui.activity.ResultBarcodeActivity;
import ru.ppr.cppk.ui.activity.RfidResultActivity;
import ru.ppr.cppk.ui.activity.closeshift.interactor.CompletePdRepealEventInteractor;
import ru.ppr.cppk.ui.activity.decrementtrip.sharedstorage.DecrementTripDataStorage;
import ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo.dialog.TripSaleStartComponent;
import ru.ppr.cppk.ui.fragment.OpenShiftSettingsFragment;
import ru.ppr.cppk.ui.fragment.pd.countrips.CountTripsPdActivityLogic;
import ru.ppr.cppk.ui.fragment.pd.pdwithplace.PdWithPlaceActivityLogic;
import ru.ppr.cppk.ui.fragment.pd.simple.SimplePdActivityLogic;
import ru.ppr.cppk.ui.helper.TicketTypeStringifier;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.AccessRuleRepository;
import ru.ppr.nsi.repository.AccessSchemeRepository;
import ru.ppr.nsi.repository.CalendarRepository;
import ru.ppr.nsi.repository.CarrierRepository;
import ru.ppr.nsi.repository.DirectionRepository;
import ru.ppr.nsi.repository.ExemptionGroupRepository;
import ru.ppr.nsi.repository.ExemptionOrganizationRepository;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.ExemptionsToRepository;
import ru.ppr.nsi.repository.FineRepository;
import ru.ppr.nsi.repository.ProcessingFeeRepository;
import ru.ppr.nsi.repository.ProductionSectionRepository;
import ru.ppr.nsi.repository.ProhibitedTicketTypeForExemptionCategoryRepository;
import ru.ppr.nsi.repository.RegionCalendarRepository;
import ru.ppr.nsi.repository.RegionRepository;
import ru.ppr.nsi.repository.ServiceFeeRepository;
import ru.ppr.nsi.repository.SmartCardCancellationReasonRepository;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.StationToTariffZoneRepository;
import ru.ppr.nsi.repository.StationTransferRouteRepository;
import ru.ppr.nsi.repository.StationsOnRouteRepository;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.nsi.repository.TicketCategoryRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;
import ru.ppr.nsi.repository.TicketTypesValidityTimesRepository;
import ru.ppr.nsi.repository.TrainCategoryRepository;
import ru.ppr.nsi.repository.VersionRepository;
import ru.ppr.rfid.IRfid;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.repository.SmartCardStopListItemRepository;

/**
 * Di приложения.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(Globals app);

    Globals app();

    Context context();

    MvpProcessor mvpProcessor();

    NsiVersionManager nsiVersionManager();

    EdsManager edsManager();

    EdsManagerWrapper edsManagerWrapper();

    EdsManagerConfigSyncronizer edsManagerConfigSyncronizer();

    LocalDbTransaction localDbTransaction();

    ScreenManager screenManager();

    BarcodeManager barcodeManager();

    IBarcodeReader barcodeReader();

    PosTerminalFactory posTerminalFactory();

    RfidManager rfidManager();

    IRfid rfid();

    BatteryManager batteryManager();

    ShiftManager shiftManager();

    PosManager posManager();

    EventBuilder eventBuilder();

    UserSessionInfo userSessionInfo();

    DeviceSessionInfo deviceSessionInfo();

    LogEventBuilder logEventBuilder();

    LocalDaoSession localDaoSession();

    NsiDaoSession nsiDaoSession();

    SecurityDaoSession securityDaoSession();

    ReadBarcodeTaskFactory readBarcodeTaskFactory();

    FindCardTaskFactory findCardTaskFactory();

    PdEncoderFactory pdEncoderFactory();

    PdDecoderFactory pdDecoderFactory();

    PassageMarkEncoderFactory passageMarkEncoderFactory();

    PassageMarkDecoderFactory passageMarkDecoderFactory();

    SamAuthorizationStrategyFactory samAuthorizationStrategyFactory();

    ServiceTicketControlCardDataStorage controlBscCardDataStorage();

    PdControlBarcodeDataStorage pdControlBarcodeDataStorage();

    PdControlCardDataStorage pdControlCardDataStorage();

    DecrementTripDataStorage decrementTripDataStorage();

    UiThread uiThread();

    ServiceDataDecoderFactory serviceDataDecoderFactory();

    CoverageAreaDecoderFactory coverageAreaDecoderFactory();

    CoverageAreaListDecoderFactory coverageAreaListDecoderFactory();

    BeepPlayer beepPlayer();

    PermissionChecker permissionChecker();

    CommonSettings commonSettings();

    PrivateSettingsHolder privateSettingsHolder();

    PrivateSettings privateSettings();

    AccessRuleRepository accessRuleRepository();

    AccessSchemeRepository accessSchemeRepository();

    CalendarRepository calendarRepository();

    CarrierRepository carrierRepository();

    ExemptionGroupRepository exemptionGroupRepository();

    ExemptionOrganizationRepository exemptionOrganizationRepository();

    ExemptionRepository exemptionRepository();

    ExemptionsToRepository exemptionsToRepository();

    FineRepository fineRepository();

    ProcessingFeeRepository processingFeeRepository();

    ProductionSectionRepository productionSectionRepository();

    ProhibitedTicketTypeForExemptionCategoryRepository prohibitedTicketTypeForExemptionCategoryRepository();

    RegionCalendarRepository regionCalendarRepository();

    RegionRepository regionRepository();

    ServiceFeeRepository serviceFeeRepository();

    SmartCardCancellationReasonRepository smartCardCancellationReasonRepository();

    StationRepository stationRepository();

    StationToTariffZoneRepository stationToTariffZoneRepository();

    StationsOnRouteRepository stationsOnRouteRepository();

    StationTransferRouteRepository stationTransferRouteRepository();

    TariffPlanRepository tariffPlanRepository();

    TariffRepository tariffRepository();

    TicketCategoryRepository ticketCategoryRepository();

    TicketTypeRepository ticketTypeRepository();

    TicketTypesValidityTimesRepository ticketTypesValidityTimesRepository();

    TrainCategoryRepository trainCategoryRepository();

    VersionRepository versionRepository();

    ServiceTicketControlEventRepository serviceTicketControlEventRepository();

    SmartCardStopListItemRepository smartCardStopListItemRepository();

    PrivateSettingsRepository privateSettingsRepository();

    DirectionRepository directionRepository();

    LocalDbVersionRepository localDbVersionRepository();

    UpdateEventRepository updateEventRepository();

    PdHandler pdHandler();

    TransferRouteChecker transferRouteChecker();

    PtkModeChecker ptkModeChecker();

    TransferPdChecker transferPdChecker();

    ValidAndControlNeededChecker validAndControlNeededChecker();

    PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder();

    FioFormatter fioFormatter();

    FioNormalizer fioNormalizer();

    TicketCategoryChecker ticketCategoryChecker();

    TicketTypeChecker ticketTypeChecker();

    CommonMenuViewManager commonMenuViewManager();

    SplashViewManager splashViewManager();

    AirplaneModeManager airplaneModeManager();

    StrategyCheckFactory strategyCheckFactory();

    NeedCreateControlEventChecker needCreateControlEventChecker();

    CommonSettingsStorage commonSettingsStorage();

    BankTransactionEventCreator bankTransactionEventCreator();

    PdSaleEnvFactory pdSaleEnvFactory();

    CriticalNsiChecker criticalNsiChecker();

    TicketTypeValidityPeriodCalculator ticketTypeValidityPeriodCalculator();

    TransferSaleButtonDetector transferSaleButtonDetector();

    FileCleaner fileCleaner();

    PrinterManager printerManager();

    BarcodeBuilder barcodeBuilder();

    FilePathProvider filePathProvider();

    IBluetoothManager blIBluetoothManager();

    DocumentNumberProvider documentNumberProvider();

    OperationFactory operationFactory();

    FiscalDocStateSynchronizer fiscalDocStateSynchronizer();

    FiscalDocStateSyncChecker fiscalDocStateSyncChecker();

    Toaster toaster();

    RecentStationsStatistics recentStationsStatistics();

    AppVersionUpdateRegister appVersionUpdateRegister();

    CommonSettingsTempStorage commonSettingsTempStorage();

    FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder();

    PaperUsageCounter paperUsageCounter();

    FullBackupCreator fullBackupCreator();

    SyncBackupCreator syncBackupCreator();

    FullBackupRestorer fullBackupRestorer();

    NsiBackupCreator nsiBackupCreator();

    NsiBackupRestorer nsiBackupRestorer();

    LocalDbBackupCreator localDbBackupCreator();

    LocalDbBackupRestorer localDbBackupRestorer();

    SecurityBackupRestorer securityBackupRestorer();

    SftBackupRestorer sftBackupRestorer();

    SftBackupCreator sftBackupCreator();

    SecurityBackupCreator securityBackupCreator();

    PrinterDbBackupCreator printerDbBackupCreator();

    PrinterDbBackupRestorer printerDbBackupRestorer();

    LogBackupCreator logBackupCreator();

    CompletePdRepealEventInteractor completePdRepealEventInteractor();

    CacheUpdater cacheUpdater();

    TicketTypeStringifier ticketTypeStringifier();

    TestPdSaleDocumentFactory testPdSaleDocumentFactory();

    TestPdSaleDocumentStateSyncronizer testPdSaleDocumentStateSyncronizer();

    PrintTestCheckInteractor printTestCheckInteractor();

    UpdateEventCreator updateEventCreator();

    MonthEventCreator monthEventCreator();

    ShiftEventCreator shiftEventCreator();

    TicketTapeEventCreator ticketTapeEventCreator();

    CashRegisterEventCreator cashRegisterEventCreator();

    DeviceIdChecker deviceIdChecker();

    PDSignChecker pdSignChecker();

    TicketControlEventCreator ticketControlEventCreator();

    OneOffAndSeasonForPeriodChecker oneOffAndSeasonForPeriodChecker();

    PdValidityPeriodCalculator pdValidityPeriodCalculator();

    SeasonForDaysTicketForWriteChecker seasonForDaysTicketChecker();

    PdVersionChecker pdVersionChecker();

    TicketTapeRestChecker ticketTapeRestChecker();

    TicketTapeChecker ticketTapeChecker();

    Resources resources();

    void inject(SimplePdActivityLogic simplePdActivityLogic);

    void inject(CountTripsPdActivityLogic countTripsPdActivityLogic);

    void inject(PdWithPlaceActivityLogic pdWithPlaceActivityLogic);

    void inject(RfidResultActivity rfidResultActivity);

    void inject(ResultBarcodeActivity resultBarcodeActivity);

    void inject(OpenShiftSettingsFragment openShiftSettingsFragment);
}
