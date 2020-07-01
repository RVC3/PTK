package ru.ppr.cppk.sync.loader.baseEntities;

import android.database.Cursor;
import android.support.v4.util.Pair;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.TicketSaleReturnEventBaseDao;
import ru.ppr.cppk.sync.kpp.baseEntities.TicketSaleReturnEventBase;
import ru.ppr.cppk.sync.kpp.model.ParentTicketInfo;
import ru.ppr.cppk.sync.kpp.model.local.TicketType;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.model.AdditionalInfoForEttLoader;
import ru.ppr.cppk.sync.loader.model.BankCardPaymentLoader;
import ru.ppr.cppk.sync.loader.model.CheckLoader;
import ru.ppr.cppk.sync.loader.model.ExemptionLoader;
import ru.ppr.cppk.sync.loader.model.FeeLoader;
import ru.ppr.cppk.sync.loader.model.LegalEntityLoader;
import ru.ppr.cppk.sync.loader.model.ParentTicketInfoLoader;
import ru.ppr.cppk.sync.loader.model.PriceLoader;
import ru.ppr.cppk.sync.loader.model.SeasonTicketLoader;
import ru.ppr.cppk.sync.loader.model.StationDeviceLoader;
import ru.ppr.cppk.sync.loader.model.TicketTypeLoader;
import ru.ppr.cppk.sync.loader.model.TrainInfoLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class TicketSaleReturnEventBaseLoader extends BaseLoader {

    private final TicketEventBaseLoader ticketEventBaseLoader;

    private final ParentTicketInfoLoader parentTicketInfoLoader;
    private final LegalEntityLoader legalEntityLoader;
    private final ExemptionLoader exemptionLoader;
    private final CheckLoader checkLoader;
    private final TrainInfoLoader trainInfoLoader;
    private final SeasonTicketLoader seasonTicketLoader;
    private final AdditionalInfoForEttLoader additionalInfoForEttLoader;
    private final PriceLoader priceLoader;
    private final FeeLoader feeLoader;
    private final BankCardPaymentLoader bankCardPaymentLoader;
    private final TicketTypeLoader ticketTypeLoader;
    private final StationDeviceLoader stationDeviceLoader;

    public TicketSaleReturnEventBaseLoader(LocalDaoSession localDaoSession,
                                           NsiDaoSession nsiDaoSession,
                                           TicketEventBaseLoader ticketEventBaseLoader,
                                           ParentTicketInfoLoader parentTicketInfoLoader,
                                           LegalEntityLoader legalEntityLoader,
                                           ExemptionLoader exemptionLoader,
                                           CheckLoader checkLoader,
                                           TrainInfoLoader trainInfoLoader,
                                           SeasonTicketLoader seasonTicketLoader,
                                           AdditionalInfoForEttLoader additionalInfoForEttLoader,
                                           PriceLoader priceLoader,
                                           FeeLoader feeLoader,
                                           BankCardPaymentLoader bankCardPaymentLoader,
                                           TicketTypeLoader ticketTypeLoader,
                                           StationDeviceLoader stationDeviceLoader) {
        super(localDaoSession, nsiDaoSession);
        this.ticketEventBaseLoader = ticketEventBaseLoader;
        this.parentTicketInfoLoader = parentTicketInfoLoader;
        this.legalEntityLoader = legalEntityLoader;
        this.exemptionLoader = exemptionLoader;
        this.checkLoader = checkLoader;
        this.trainInfoLoader = trainInfoLoader;
        this.seasonTicketLoader = seasonTicketLoader;
        this.additionalInfoForEttLoader = additionalInfoForEttLoader;
        this.priceLoader = priceLoader;
        this.feeLoader = feeLoader;
        this.bankCardPaymentLoader = bankCardPaymentLoader;
        this.ticketTypeLoader = ticketTypeLoader;
        this.stationDeviceLoader = stationDeviceLoader;
    }

    public static class Columns {
        static final Column EXEMPTION_ID = new Column(0, TicketSaleReturnEventBaseDao.Properties.ExemptionId);
        static final Column KIND = new Column(1, TicketSaleReturnEventBaseDao.Properties.Kind);
        static final Column SEASON_TICKET_ID = new Column(2, TicketSaleReturnEventBaseDao.Properties.SeasonTicketId);
        static final Column IS_ONE_TIME_TICKET = new Column(3, TicketSaleReturnEventBaseDao.Properties.IsOneTimeTicket);
        static final Column ADDITIONAL_INFO_FOR_ETT_ID = new Column(4, TicketSaleReturnEventBaseDao.Properties.AdditionInfoForEttId);
        static final Column PAYMENT_TYPE_CODE = new Column(5, TicketSaleReturnEventBaseDao.Properties.PaymentTypeCode);
        static final Column BANK_TRANSACTION_CASH_REGISTER_EVENT_ID = new Column(6, TicketSaleReturnEventBaseDao.Properties.BankTransactionCashRegisterEventId);
        static final Column CHECK_ID = new Column(7, TicketSaleReturnEventBaseDao.Properties.CheckId);
        static final Column FEE_ID = new Column(8, TicketSaleReturnEventBaseDao.Properties.FeeId);
        static final Column PARENT_TICKET_INFO_ID = new Column(9, TicketSaleReturnEventBaseDao.Properties.ParentTicketId);

        public static Column[] allForSale = new Column[]{
                EXEMPTION_ID,
                KIND,
                SEASON_TICKET_ID,
                IS_ONE_TIME_TICKET,
                ADDITIONAL_INFO_FOR_ETT_ID,
                PAYMENT_TYPE_CODE,
                BANK_TRANSACTION_CASH_REGISTER_EVENT_ID,
                CHECK_ID,
                FEE_ID,
                PARENT_TICKET_INFO_ID
        };

        public static Column[] allForReturn = new Column[]{
                EXEMPTION_ID,
                KIND,
                SEASON_TICKET_ID,
                IS_ONE_TIME_TICKET,
                ADDITIONAL_INFO_FOR_ETT_ID,
                PAYMENT_TYPE_CODE,
                BANK_TRANSACTION_CASH_REGISTER_EVENT_ID,
                CHECK_ID,
                FEE_ID
        };
    }

    /**
     * Заполнить поля для сущности TicketSaleReturnEventBase
     *
     * @param ticketSaleReturnEventBase - сама заполняемая моделька
     * @param forReturn                 - флаг того, что выгрзука для событий аннулирования
     * @param returnWorkingShiftId      - идентификатор cashRegisterWorkingShiftId для события аннулирования
     * @param cursor                    - курсор
     * @param offset                    - смещение в курсоре
     */
    public void fill(TicketSaleReturnEventBase ticketSaleReturnEventBase, boolean forReturn, long returnWorkingShiftId, Cursor cursor, Offset offset) {

        //получаем все данные сущности из курсора
        long parentTicketInfoId = forReturn ? 0 : cursor.getLong(offset.value + Columns.PARENT_TICKET_INFO_ID.index);
        long exemptionId = cursor.getLong(offset.value + Columns.EXEMPTION_ID.index);
        ticketSaleReturnEventBase.Kind = cursor.getInt(offset.value + Columns.KIND.index);
        long seasonTicketId = cursor.getLong(offset.value + Columns.SEASON_TICKET_ID.index);
        ticketSaleReturnEventBase.IsOneTimeTicket = cursor.getInt(offset.value + Columns.IS_ONE_TIME_TICKET.index) == 1;
        long additionalInfoForEttId = cursor.getLong(offset.value + Columns.ADDITIONAL_INFO_FOR_ETT_ID.index);
        ticketSaleReturnEventBase.PaymentMethod = cursor.getInt(offset.value + Columns.PAYMENT_TYPE_CODE.index);
        long bankTransactionCashRegisterEventId = cursor.getLong(offset.value + Columns.BANK_TRANSACTION_CASH_REGISTER_EVENT_ID.index);
        long checkId = cursor.getLong(offset.value + Columns.CHECK_ID.index);
        long feeId = cursor.getLong(offset.value + Columns.FEE_ID.index);

        //делаем смещение в курсоре
        if (forReturn)
            offset.value += Columns.allForReturn.length;
        else
            offset.value += Columns.allForSale.length;

        //заполняем базовые сущности верхнего уровня
        ticketEventBaseLoader.fill(ticketSaleReturnEventBase, false, forReturn, returnWorkingShiftId, cursor, offset);

        //грузим child-сущности из JOIN-а
        ticketSaleReturnEventBase.Carrier = legalEntityLoader.load(cursor, offset);
        ticketSaleReturnEventBase.TrainInfo = trainInfoLoader.load(cursor, offset);
        //тут нужен тариф, чтобы достать из него Tax
        int versionId = ticketSaleReturnEventBase.VersionId;
        long ticketTypeCode = ticketSaleReturnEventBase.TypeCode;
        Pair<TicketType, Integer> ticketTypeResp = ticketTypeLoader.loadTicketType(ticketTypeCode, versionId);
        ticketSaleReturnEventBase.FullPrice = priceLoader.load(cursor, offset, ticketTypeResp.first.tax);

        //заполняем модельки отдельными запросами к БД
        ticketSaleReturnEventBase.Exemption = (exemptionId > 0) ? exemptionLoader.load(exemptionId) : null;
        ticketSaleReturnEventBase.SeasonTicket = (seasonTicketId > 0) ? seasonTicketLoader.load(seasonTicketId) : null;
        ticketSaleReturnEventBase.AdditionalInfoForETT = (additionalInfoForEttId > 0) ? additionalInfoForEttLoader.load(additionalInfoForEttId) : null;
        ticketSaleReturnEventBase.BankCardPayment = (bankTransactionCashRegisterEventId > 0) ? bankCardPaymentLoader.load(bankTransactionCashRegisterEventId) : null;
        ticketSaleReturnEventBase.Check = (checkId > 0) ? checkLoader.load(checkId) : null;
        ticketSaleReturnEventBase.Fee = (feeId > 0) ? feeLoader.load(feeId) : null;
        if (forReturn) {
            //http://agile.srvdev.ru/browse/CPPKPP-25093
            ticketSaleReturnEventBase.ParentTicket = new ParentTicketInfo();
            ticketSaleReturnEventBase.ParentTicket.WayType = ticketSaleReturnEventBase.WayType;
            ticketSaleReturnEventBase.ParentTicket.SaleDateTime = ticketSaleReturnEventBase.SaleDateTime;
            ticketSaleReturnEventBase.ParentTicket.TicketNumber = ticketSaleReturnEventBase.TicketNumber;
            stationDeviceLoader.fillParentTicketForCppkTicketReturnFields(ticketSaleReturnEventBase.ParentTicket, cursor, offset);
        } else {
            ticketSaleReturnEventBase.ParentTicket = (parentTicketInfoId > 0) ? parentTicketInfoLoader.load(parentTicketInfoId) : null;
        }

    }

}