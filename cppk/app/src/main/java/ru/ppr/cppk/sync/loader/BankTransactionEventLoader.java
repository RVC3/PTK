package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BankTransactionDao;
import ru.ppr.cppk.sync.kpp.BankTransactionEvent;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterWorkingShiftEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class BankTransactionEventLoader extends BaseLoader {

    private final EventLoader eventLoader;
    private final CashRegisterWorkingShiftEventLoader cashRegisterWorkingShiftEventLoader;

    public BankTransactionEventLoader(LocalDaoSession localDaoSession,
                                      NsiDaoSession nsiDaoSession,
                                      EventLoader eventLoader,
                                      CashRegisterWorkingShiftEventLoader cashRegisterWorkingShiftEventLoader) {
        super(localDaoSession, nsiDaoSession);
        this.eventLoader = eventLoader;
        this.cashRegisterWorkingShiftEventLoader = cashRegisterWorkingShiftEventLoader;
    }

    public static class Columns {
        static final Column TRANSACTION_ID = new Column(0, BankTransactionDao.Properties.TransactionId);
        static final Column TERMINAL_NUMBER = new Column(1, BankTransactionDao.Properties.TerminalNumber);
        static final Column POINT_OF_SALE_NUMBER = new Column(2, BankTransactionDao.Properties.PointOfSaleNumber);
        static final Column MERCHANT_ID = new Column(3, BankTransactionDao.Properties.MerchantId);
        static final Column BANK_CODE = new Column(4, BankTransactionDao.Properties.BankCode);
        static final Column OPERATION_TYPE = new Column(5, BankTransactionDao.Properties.OperationType);
        static final Column OPERATION_RESULT = new Column(6, BankTransactionDao.Properties.OperationResult);
        static final Column RRN = new Column(7, BankTransactionDao.Properties.Rrn);
        static final Column AUTH_CODE = new Column(8, BankTransactionDao.Properties.AuthorizationCode);
        static final Column SMART_CARD_APPLICATION_NAME = new Column(9, BankTransactionDao.Properties.SmartCardApplicationName);
        static final Column CARD_PAN = new Column(10, BankTransactionDao.Properties.CardPan);
        static final Column CARD_EMITENT_NAME = new Column(11, BankTransactionDao.Properties.CardEmitentName);
        static final Column BANK_CHECK_NUMBER = new Column(12, BankTransactionDao.Properties.BankCheckNumber);
        static final Column TRANSACTION_DATE_TIME = new Column(13, BankTransactionDao.Properties.TransactionDateTime);
        static final Column TOTAL = new Column(14, BankTransactionDao.Properties.Total);
        static final Column CURRENCY_CODE = new Column(15, BankTransactionDao.Properties.CurrencyCode);
        static final Column CASH_REGISTER_WORKING_SHIFT_ID = new Column(16, BankTransactionDao.Properties.CashRegisterWorkingShiftId);

        public static Column[] all = new Column[]{
                TRANSACTION_ID,
                TERMINAL_NUMBER,
                POINT_OF_SALE_NUMBER,
                MERCHANT_ID,
                BANK_CODE,
                OPERATION_TYPE,
                OPERATION_RESULT,
                RRN,
                AUTH_CODE,
                SMART_CARD_APPLICATION_NAME,
                CARD_PAN,
                CARD_EMITENT_NAME,
                BANK_CHECK_NUMBER,
                TRANSACTION_DATE_TIME,
                TOTAL,
                CURRENCY_CODE,
                CASH_REGISTER_WORKING_SHIFT_ID
        };
    }

    public BankTransactionEvent load(Cursor cursor, Offset offset) {

        BankTransactionEvent bankTransactionEvent = new BankTransactionEvent();

        bankTransactionEvent.ern = cursor.getInt(offset.value + Columns.TRANSACTION_ID.index);
        bankTransactionEvent.terminalNumber = cursor.getString(offset.value + Columns.TERMINAL_NUMBER.index);
        bankTransactionEvent.pointOfSaleNumber = cursor.getString(offset.value + Columns.POINT_OF_SALE_NUMBER.index);
        bankTransactionEvent.merchantId = cursor.getString(offset.value + Columns.MERCHANT_ID.index);
        bankTransactionEvent.bankCode = cursor.getInt(offset.value + Columns.BANK_CODE.index);
        bankTransactionEvent.operationType = cursor.getInt(offset.value + Columns.OPERATION_TYPE.index);
        bankTransactionEvent.operationResult = cursor.getInt(offset.value + Columns.OPERATION_RESULT.index);
        bankTransactionEvent.rrn = cursor.getString(offset.value + Columns.RRN.index);
        bankTransactionEvent.authorizationCode = cursor.getString(offset.value + Columns.AUTH_CODE.index);
        bankTransactionEvent.smartCardApplicationName = cursor.getString(offset.value + Columns.SMART_CARD_APPLICATION_NAME.index);
        bankTransactionEvent.cardPan = cursor.getString(offset.value + Columns.CARD_PAN.index);
        bankTransactionEvent.cardEmitentName = cursor.getString(offset.value + Columns.CARD_EMITENT_NAME.index);
        bankTransactionEvent.bankCheckNumber = cursor.getInt(offset.value + Columns.BANK_CHECK_NUMBER.index);
        bankTransactionEvent.transactionDateTime = new Date(cursor.getLong(offset.value + Columns.TRANSACTION_DATE_TIME.index));
        bankTransactionEvent.total = new BigDecimal(cursor.getString(offset.value + Columns.TOTAL.index));
        bankTransactionEvent.currencyCode = cursor.getString(offset.value + Columns.CURRENCY_CODE.index);
        long cashRegisterWorkingShiftId = cursor.getLong(offset.value + Columns.CASH_REGISTER_WORKING_SHIFT_ID.index);

        offset.value += Columns.all.length;

        eventLoader.fill(bankTransactionEvent, cursor, offset);
        cashRegisterWorkingShiftEventLoader.fill(bankTransactionEvent, cashRegisterWorkingShiftId);

        return bankTransactionEvent;
    }

}