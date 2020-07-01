package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.CppkTicketReturnDao;
import ru.ppr.cppk.sync.kpp.CPPKTicketReturn;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.TicketSaleReturnEventBaseLoader;
import ru.ppr.cppk.sync.loader.model.BankCardPaymentLoader;
import ru.ppr.cppk.sync.loader.model.CheckLoader;
import ru.ppr.cppk.sync.loader.model.PriceLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class CPPKTicketReturnLoader extends BaseLoader {

    private final TicketSaleLoader ticketSaleLoader;
    private final TicketSaleReturnEventBaseLoader ticketSaleReturnEventBaseLoader;
    private final CheckLoader checkLoader;
    private final PriceLoader priceLoader;
    private final BankCardPaymentLoader bankCardPaymentLoader;

    public CPPKTicketReturnLoader(LocalDaoSession localDaoSession,
                                  NsiDaoSession nsiDaoSession,
                                  TicketSaleLoader ticketSaleLoader,
                                  TicketSaleReturnEventBaseLoader ticketSaleReturnEventBaseLoader,
                                  CheckLoader checkLoader,
                                  PriceLoader priceLoader,
                                  BankCardPaymentLoader bankCardPaymentLoader) {
        super(localDaoSession, nsiDaoSession);
        this.ticketSaleLoader = ticketSaleLoader;
        this.ticketSaleReturnEventBaseLoader = ticketSaleReturnEventBaseLoader;
        this.checkLoader = checkLoader;
        this.priceLoader = priceLoader;
        this.bankCardPaymentLoader = bankCardPaymentLoader;
    }

    public static class Columns {
        static final Column RETURN_OPERATION = new Column(0, CppkTicketReturnDao.Properties.ReturnOperationTypeCode);
        static final Column RECALL_DATETIME = new Column(1, CppkTicketReturnDao.Properties.RecallDateTime);
        static final Column RECALL_REASON = new Column(2, CppkTicketReturnDao.Properties.RecallReason);
        static final Column RETURN_PAYMENT_METHOD = new Column(3, CppkTicketReturnDao.Properties.ReturnPaymentTypeCode);
        static final Column RETURN_BANK_TRANSACTION_CASH_REGISTER_EVENT_ID = new Column(4, CppkTicketReturnDao.Properties.ReturnBankTransactionCashRegisterEventId);
        static final Column CASH_REGISTER_WORKING_SHIFT_ID = new Column(5, CppkTicketReturnDao.Properties.CashRegisterWorkingShiftId);

        public static Column[] all = new Column[]{
                RETURN_OPERATION,
                RECALL_DATETIME,
                RECALL_REASON,
                RETURN_PAYMENT_METHOD,
                RETURN_BANK_TRANSACTION_CASH_REGISTER_EVENT_ID,
                CASH_REGISTER_WORKING_SHIFT_ID
        };
    }


    public CPPKTicketReturn load(Cursor cursor, Offset offset) {

        CPPKTicketReturn cppkTicketReturn = new CPPKTicketReturn();

        // region CPPKTicketReturn
        cppkTicketReturn.operation = cursor.getInt(offset.value + Columns.RETURN_OPERATION.index);
        cppkTicketReturn.recallDateTime = new Date(cursor.getLong(offset.value + Columns.RECALL_DATETIME.index));
        cppkTicketReturn.recallReason = cursor.getString(offset.value + Columns.RECALL_REASON.index);
        cppkTicketReturn.returnPaymentMethod = cursor.getInt(offset.value + Columns.RETURN_PAYMENT_METHOD.index);
        //ReturnBankCardPayment
        int returnBankTransactionCashRegisterEventIdIndex = offset.value + Columns.RETURN_BANK_TRANSACTION_CASH_REGISTER_EVENT_ID.index;
        long returnBankTransactionCashRegisterEventId = cursor.isNull(returnBankTransactionCashRegisterEventIdIndex) ? -1 : cursor.getLong(returnBankTransactionCashRegisterEventIdIndex);
        long workingShiftId = cursor.getLong(offset.value + Columns.CASH_REGISTER_WORKING_SHIFT_ID.index);
        //все поля из сущности CPPKTicketReturn закончились, инкрементируем offset
        offset.value += Columns.all.length;
        //endregion

        // FullTicketPrice
        ticketSaleLoader.fillCppkReturnFields(cppkTicketReturn, cursor, offset);

        // RecallTicketNumber
        checkLoader.fillCppkReturnFields(cppkTicketReturn, cursor, offset);

        // SumToReturn
        priceLoader.fillCppkReturnFields(cppkTicketReturn, cursor, offset);

        //Заполним TicketNumber
        checkLoader.fillTicketEventBaseFields(cppkTicketReturn, cursor, offset);

        ticketSaleReturnEventBaseLoader.fill(cppkTicketReturn, true, workingShiftId, cursor, offset);

        //заполним мелкие модельки отдельными запросами
        cppkTicketReturn.returnBankCardPayment = (returnBankTransactionCashRegisterEventId > 0) ? bankCardPaymentLoader.load(returnBankTransactionCashRegisterEventId) : null;

        return cppkTicketReturn;
    }

}
