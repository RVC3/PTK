package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.FineSaleEventDao;
import ru.ppr.cppk.sync.kpp.FinePaidEvent;
import ru.ppr.cppk.sync.kpp.model.local.Fine;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterWorkingShiftEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.BankCardPaymentLoader;
import ru.ppr.cppk.sync.loader.model.CheckLoader;
import ru.ppr.cppk.sync.loader.model.local.FineLoader;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class FinePaidEventLoader extends BaseLoader {

    private final CheckLoader checkLoader;
    private final EventLoader eventLoader;
    private final CashRegisterWorkingShiftEventLoader cashRegisterWorkingShiftEventLoader;
    private final BankCardPaymentLoader bankCardPaymentLoader;
    private final FineLoader fineLoader;

    public FinePaidEventLoader(LocalDaoSession localDaoSession,
                               NsiDaoSession nsiDaoSession,
                               CheckLoader checkLoader,
                               EventLoader eventLoader,
                               CashRegisterWorkingShiftEventLoader cashRegisterWorkingShiftEventLoader,
                               BankCardPaymentLoader bankCardPaymentLoader,
                               FineLoader fineLoader) {
        super(localDaoSession, nsiDaoSession);
        this.checkLoader = checkLoader;
        this.eventLoader = eventLoader;
        this.cashRegisterWorkingShiftEventLoader = cashRegisterWorkingShiftEventLoader;
        this.bankCardPaymentLoader = bankCardPaymentLoader;
        this.fineLoader = fineLoader;
    }

    public static class Columns {
        static final Column FINE_CODE = new Column(0, FineSaleEventDao.Properties.FineCode);
        static final Column OPERATION_DATE_TIME = new Column(1, FineSaleEventDao.Properties.OperationDateTime);
        static final Column AMOUNT = new Column(2, FineSaleEventDao.Properties.Amount);
        static final Column PAYMENT_METHOD_CODE = new Column(3, FineSaleEventDao.Properties.PaymentMethodCode);
        static final Column BANK_TRANSACTION_EVENT_ID = new Column(4, FineSaleEventDao.Properties.BankTransactionEventId);
        static final Column CASH_REGISTER_WORKING_SHIFT_ID = new Column(5, FineSaleEventDao.Properties.CashRegisterWorkingShiftId);

        public static Column[] all = new Column[]{
                FINE_CODE,
                OPERATION_DATE_TIME,
                AMOUNT,
                PAYMENT_METHOD_CODE,
                BANK_TRANSACTION_EVENT_ID,
                CASH_REGISTER_WORKING_SHIFT_ID
        };
    }

    public FinePaidEvent load(Cursor cursor, Offset offset) {

        FinePaidEvent finePaidEvent = new FinePaidEvent();

        finePaidEvent.fineCode = cursor.getInt(offset.value + Columns.FINE_CODE.index);
        finePaidEvent.operationDateTime = new Date(cursor.getLong(offset.value + Columns.OPERATION_DATE_TIME.index));
        finePaidEvent.amount = new BigDecimal(cursor.getString(offset.value + Columns.AMOUNT.index));
        finePaidEvent.paymentType = cursor.getInt(offset.value + Columns.PAYMENT_METHOD_CODE.index);
        long bankTransactionEventId = cursor.getLong(offset.value + Columns.BANK_TRANSACTION_EVENT_ID.index);
        long cashRegisterWorkingShiftId = cursor.getLong(offset.value + Columns.CASH_REGISTER_WORKING_SHIFT_ID.index);

        offset.value += Columns.all.length;

        //Заполним docNumber
        checkLoader.fillFinePaidEventFields(finePaidEvent, cursor, offset);

        eventLoader.fill(finePaidEvent, cursor, offset);
        cashRegisterWorkingShiftEventLoader.fill(finePaidEvent, cashRegisterWorkingShiftId);

        //заполним чек
        finePaidEvent.check = checkLoader.load(cursor, offset);

        finePaidEvent.bankCardPayment = bankTransactionEventId > 0 ? bankCardPaymentLoader.load(bankTransactionEventId) : null;

        Fine fine = fineLoader.loadFine(finePaidEvent.fineCode, finePaidEvent.VersionId).first;
        finePaidEvent.ndsPercent = fine.ndsPercent;
        finePaidEvent.nds = Decimals.getVATValueIncludedFromRate(finePaidEvent.amount, BigDecimal.valueOf(fine.ndsPercent), Decimals.RoundMode.HUNDREDTH);

        return finePaidEvent;
    }

}