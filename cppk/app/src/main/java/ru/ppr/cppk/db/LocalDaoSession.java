package ru.ppr.cppk.db;

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
import ru.ppr.database.DbOpenHelper;
import ru.ppr.database.references.References;

/**
 * Высокоуровневая обертка над локальной БД.
 * Точка входа в слой для работы с локальной БД.
 * Объединяет в себе все мелкие DAO-объекты.
 * Никак не управляет подключением!
 * В случае закрытия соединения с БД через {@link DbOpenHelper}
 * и повторного получения БД через {@link DbOpenHelper#getReadableDatabase()}
 * нужно создавать новый объект {@link LocalDaoSession} на основе {@link Database}
 *
 * @author Aleksandr Brazhkin
 */
public interface LocalDaoSession extends DaoSession {

    /**
     * Возвращает локальную БД
     *
     * @return Локальная БД
     */
    Database getLocalDb();

    // список таблиц
    Map<String, BaseEntityDao> getEntities();

    // регистрирует таблицу
    void registerEntity(BaseEntityDao entity);

    // Возвращает информацию о связях таблиц
    References getReferences();

    CppkTicketReturnDao getCppkTicketReturnDao();

    ShiftEventDao getShiftEventDao();

    ParentTicketInfoDao getParentTicketInfoDao();

    CashRegisterEventDao getCashRegisterEventDao();

    TestTicketDao getTestTicketDao();

    MonthEventDao getMonthEventDao();

    AuditTrailEventDao getAuditTrailEventDao();

    CppkTicketControlsDao getCppkTicketControlsDao();

    CppkTicketSaleDao getCppkTicketSaleDao();

    CheckDao getCheckDao();

    TicketTapeEventDao getTicketTapeEventDao();

    PrintReportEventDao getPrintReportEventDao();

    BankTransactionDao getBankTransactionDao();

    TerminalDayDao getTerminalDayDao();

    UpdateEventDao getUpdateEventDao();

    TicketEventBaseDao getTicketEventBaseDao();

    TicketSaleReturnEventBaseDao getTicketSaleReturnEventBaseDao();

    CPPKTicketReSignDao getCppkTicketReSignDao();

    SmartCardDao getSmartCardDao();

    PriceDao getPriceDao();

    CPPKServiceSaleDao getCppkServiceSaleDao();

    AdditionalInfoForEttDao getAdditionalInfoForEttDao();

    PaperUsageDao getPaperUsageDao();

    SentEventsDao getSentEventsDao();

    FineSaleEventDao getFineSaleEventDao();

    EventDao getEventDao();

    StationDeviceDao getStationDeviceDao();

    CouponReadEventDao getCouponReadEventDao();

    ServiceTicketControlEventDao getServiceTicketControlEventDao();

    FeeDao getFeeDao();

    ExemptionDao exemptionDao();

    CashierDao cashierDao();

    LegalEntityDao legalEntityDao();

    CashRegisterDao cashRegisterDao();

    SeasonTicketDao seasonTicketDao();

    TrainInfoDao trainInfoDao();

    LogEventDao logEventDao();

    LocalDbVersionDao localDbVersionDao();

    CommonSettingsDao commonSettingsDao();

    PrivateSettingsDao privateSettingsDao();
}
