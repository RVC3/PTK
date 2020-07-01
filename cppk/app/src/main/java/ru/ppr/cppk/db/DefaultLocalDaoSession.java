package ru.ppr.cppk.db;

import java.util.HashMap;
import java.util.Map;

import ru.ppr.cppk.db.local.AdditionalInfoForEttDao;
import ru.ppr.cppk.db.local.AuditTrailEventDao;
import ru.ppr.cppk.db.local.BankTransactionDao;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CPPKServiceSaleDao;
import ru.ppr.cppk.db.local.CPPKTicketReSignDao;
import ru.ppr.cppk.db.local.CashRegisterDao;
import ru.ppr.cppk.db.local.CashRegisterEventDao;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.db.local.CashierDao;
import ru.ppr.cppk.db.local.CheckDao;
import ru.ppr.cppk.db.local.CommonSettingsDao;
import ru.ppr.cppk.db.local.CouponReadEventDao;
import ru.ppr.cppk.db.local.CppkTicketControlsDao;
import ru.ppr.cppk.db.local.CppkTicketReturnDao;
import ru.ppr.cppk.db.local.CppkTicketSaleDao;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.db.local.ExemptionDao;
import ru.ppr.cppk.db.local.FeeDao;
import ru.ppr.cppk.db.local.FineSaleEventDao;
import ru.ppr.cppk.db.local.LegalEntityDao;
import ru.ppr.cppk.db.local.LocalDbVersionDao;
import ru.ppr.cppk.db.local.LogEventDao;
import ru.ppr.cppk.db.local.MonthEventDao;
import ru.ppr.cppk.db.local.PaperUsageDao;
import ru.ppr.cppk.db.local.ParentTicketInfoDao;
import ru.ppr.cppk.db.local.PriceDao;
import ru.ppr.cppk.db.local.PrintReportEventDao;
import ru.ppr.cppk.db.local.PrivateSettingsDao;
import ru.ppr.cppk.db.local.SeasonTicketDao;
import ru.ppr.cppk.db.local.SentEventsDao;
import ru.ppr.cppk.db.local.ServiceTicketControlEventDao;
import ru.ppr.cppk.db.local.SmartCardDao;
import ru.ppr.cppk.db.local.StationDeviceDao;
import ru.ppr.cppk.db.local.TerminalDayDao;
import ru.ppr.cppk.db.local.TestTicketDao;
import ru.ppr.cppk.db.local.TicketEventBaseDao;
import ru.ppr.cppk.db.local.TicketSaleReturnEventBaseDao;
import ru.ppr.cppk.db.local.TicketTapeEventDao;
import ru.ppr.cppk.db.local.TrainInfoDao;
import ru.ppr.cppk.db.local.UpdateEventDao;
import ru.ppr.database.Database;
import ru.ppr.database.references.References;

/**
 * @author Aleksandr Brazhkin
 */
public class DefaultLocalDaoSession implements LocalDaoSession {

    /**
     * Локальная БД
     */
    private final Database db;

    private final Map<String, BaseEntityDao> entities;
    private final References references;

    ///////////////////////////////////
    private final ShiftEventDao shiftEventDao;
    private final MonthEventDao monthEventDao;
    private final CppkTicketControlsDao cppkTicketControlsDao;
    private final CppkTicketSaleDao cppkTicketSaleDao;
    private final CppkTicketReturnDao cppkTicketReturnDao;
    private final CheckDao checkDao;
    private final BankTransactionDao bankTransactionDao;
    private final TestTicketDao testTicketDao;
    private final TicketTapeEventDao ticketTapeEventDao;
    private final PrintReportEventDao printReportEventDao;
    private final AuditTrailEventDao auditTrailEventDao;
    private final TerminalDayDao terminalDayDao;
    private final UpdateEventDao updateEventDao;
    private final CashRegisterEventDao cashRegisterEventDao;
    private final SmartCardDao smartCardDao;
    private final TicketEventBaseDao ticketEventBaseDao;
    private final TicketSaleReturnEventBaseDao ticketSaleReturnEventBaseDao;
    private final ParentTicketInfoDao parentTicketInfoDao;
    private final CPPKTicketReSignDao cppkTicketReSignDao;
    private final PriceDao priceDao;
    private final CPPKServiceSaleDao cppkServiceSaleDao;
    private final AdditionalInfoForEttDao additionalInfoForEttDao;
    private final PaperUsageDao paperUsageDao;
    private final SentEventsDao sentEventsDao;
    private final FineSaleEventDao fineSaleEventDao;
    private final EventDao eventDao;
    private final StationDeviceDao stationDeviceDao;
    private final CouponReadEventDao couponReadEventDao;
    private final ServiceTicketControlEventDao serviceTicketControlEventDao;
    private final FeeDao feeDao;
    private final ExemptionDao exemptionDao;
    private final CashierDao cashierDao;
    private final LegalEntityDao legalEntityDao;
    private final CashRegisterDao cashRegisterDao;
    private final SeasonTicketDao seasonTicketDao;
    private final TrainInfoDao trainInfoDao;
    private final LogEventDao logEventDao;
    private final LocalDbVersionDao localDbVersionDao;
    private final CommonSettingsDao commonSettingsDao;
    private final PrivateSettingsDao privateSettingsDao;

    public DefaultLocalDaoSession(Database db) {
        this.db = db;
        this.references = new References();
        this.entities = new HashMap<>();

        shiftEventDao = new ShiftEventDao(this);
        monthEventDao = new MonthEventDao(this);
        cppkTicketControlsDao = new CppkTicketControlsDao(this);
        cppkTicketSaleDao = new CppkTicketSaleDao(this);
        checkDao = new CheckDao(this);
        bankTransactionDao = new BankTransactionDao(this);
        cppkTicketReturnDao = new CppkTicketReturnDao(this);
        testTicketDao = new TestTicketDao(this);
        ticketTapeEventDao = new TicketTapeEventDao(this);
        printReportEventDao = new PrintReportEventDao(this);
        auditTrailEventDao = new AuditTrailEventDao(this);
        terminalDayDao = new TerminalDayDao(this);
        updateEventDao = new UpdateEventDao(this);
        cashRegisterEventDao = new CashRegisterEventDao(this);
        smartCardDao = new SmartCardDao(this);
        ticketEventBaseDao = new TicketEventBaseDao(this);
        ticketSaleReturnEventBaseDao = new TicketSaleReturnEventBaseDao(this);
        parentTicketInfoDao = new ParentTicketInfoDao(this);
        cppkTicketReSignDao = new CPPKTicketReSignDao(this);
        priceDao = new PriceDao(this);
        cppkServiceSaleDao = new CPPKServiceSaleDao(this);
        additionalInfoForEttDao = new AdditionalInfoForEttDao(this);
        paperUsageDao = new PaperUsageDao(this);
        sentEventsDao = new SentEventsDao(this);
        fineSaleEventDao = new FineSaleEventDao(this);
        eventDao = new EventDao(this);
        stationDeviceDao = new StationDeviceDao(this);
        couponReadEventDao = new CouponReadEventDao(this);
        serviceTicketControlEventDao = new ServiceTicketControlEventDao(this);
        feeDao = new FeeDao(this);
        exemptionDao = new ExemptionDao(this);
        cashierDao = new CashierDao(this);
        legalEntityDao = new LegalEntityDao(this);
        cashRegisterDao = new CashRegisterDao(this);
        seasonTicketDao = new SeasonTicketDao(this);
        trainInfoDao = new TrainInfoDao(this);
        logEventDao = new LogEventDao(this);
        localDbVersionDao = new LocalDbVersionDao(this);
        commonSettingsDao = new CommonSettingsDao(this);
        privateSettingsDao = new PrivateSettingsDao(this);

        // проверка корректности регистрации ссылок
        references.checkReferencesDeclaration(entities.values());
    }

    @Override
    public Database getLocalDb() {
        return db;
    }

    @Override
    public Map<String, BaseEntityDao> getEntities() {
        return entities;
    }

    @Override
    public void registerEntity(BaseEntityDao entity) {
        entities.put(entity.getTableName(), entity);
    }

    @Override
    public References getReferences() {
        return references;
    }

    @Override
    public void beginTransaction() {
        db.beginTransaction();
    }

    @Override
    public void endTransaction() {
        db.endTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        db.setTransactionSuccessful();
    }

    @Override
    public ShiftEventDao getShiftEventDao() {
        return shiftEventDao;
    }

    @Override
    public MonthEventDao getMonthEventDao() {
        return monthEventDao;
    }

    @Override
    public CppkTicketControlsDao getCppkTicketControlsDao() {
        return cppkTicketControlsDao;
    }

    @Override
    public CppkTicketSaleDao getCppkTicketSaleDao() {
        return cppkTicketSaleDao;
    }

    @Override
    public CppkTicketReturnDao getCppkTicketReturnDao() {
        return cppkTicketReturnDao;
    }

    @Override
    public CheckDao getCheckDao() {
        return checkDao;
    }

    @Override
    public BankTransactionDao getBankTransactionDao() {
        return bankTransactionDao;
    }

    @Override
    public TestTicketDao getTestTicketDao() {
        return testTicketDao;
    }

    @Override
    public TicketTapeEventDao getTicketTapeEventDao() {
        return ticketTapeEventDao;
    }

    @Override
    public PrintReportEventDao getPrintReportEventDao() {
        return printReportEventDao;
    }

    @Override
    public AuditTrailEventDao getAuditTrailEventDao() {
        return auditTrailEventDao;
    }

    @Override
    public TerminalDayDao getTerminalDayDao() {
        return terminalDayDao;
    }

    @Override
    public UpdateEventDao getUpdateEventDao() {
        return updateEventDao;
    }

    @Override
    public CashRegisterEventDao getCashRegisterEventDao() {
        return cashRegisterEventDao;
    }

    @Override
    public SmartCardDao getSmartCardDao() {
        return smartCardDao;
    }

    @Override
    public TicketEventBaseDao getTicketEventBaseDao() {
        return ticketEventBaseDao;
    }

    @Override
    public TicketSaleReturnEventBaseDao getTicketSaleReturnEventBaseDao() {
        return ticketSaleReturnEventBaseDao;
    }

    @Override
    public ParentTicketInfoDao getParentTicketInfoDao() {
        return parentTicketInfoDao;
    }

    @Override
    public CPPKTicketReSignDao getCppkTicketReSignDao() {
        return cppkTicketReSignDao;
    }

    @Override
    public PriceDao getPriceDao() {
        return priceDao;
    }

    @Override
    public CPPKServiceSaleDao getCppkServiceSaleDao() {
        return cppkServiceSaleDao;
    }

    @Override
    public AdditionalInfoForEttDao getAdditionalInfoForEttDao() {
        return additionalInfoForEttDao;
    }

    @Override
    public PaperUsageDao getPaperUsageDao() {
        return paperUsageDao;
    }

    @Override
    public SentEventsDao getSentEventsDao() {
        return sentEventsDao;
    }

    @Override
    public FineSaleEventDao getFineSaleEventDao() {
        return fineSaleEventDao;
    }

    @Override
    public EventDao getEventDao() {
        return eventDao;
    }

    @Override
    public StationDeviceDao getStationDeviceDao() {
        return stationDeviceDao;
    }

    @Override
    public CouponReadEventDao getCouponReadEventDao() {
        return couponReadEventDao;
    }

    @Override
    public ServiceTicketControlEventDao getServiceTicketControlEventDao() {
        return serviceTicketControlEventDao;
    }

    @Override
    public FeeDao getFeeDao() {
        return feeDao;
    }

    @Override
    public ExemptionDao exemptionDao() {
        return exemptionDao;
    }

    @Override
    public CashierDao cashierDao() {
        return cashierDao;
    }

    @Override
    public LegalEntityDao legalEntityDao() {
        return legalEntityDao;
    }

    @Override
    public CashRegisterDao cashRegisterDao() {
        return cashRegisterDao;
    }

    @Override
    public SeasonTicketDao seasonTicketDao() {
        return seasonTicketDao;
    }

    @Override
    public TrainInfoDao trainInfoDao() {
        return trainInfoDao;
    }

    @Override
    public LogEventDao logEventDao() {
        return logEventDao;
    }

    @Override
    public LocalDbVersionDao localDbVersionDao() {
        return localDbVersionDao;
    }

    @Override
    public CommonSettingsDao commonSettingsDao() {
        return commonSettingsDao;
    }

    @Override
    public PrivateSettingsDao privateSettingsDao() {
        return privateSettingsDao;
    }
}
