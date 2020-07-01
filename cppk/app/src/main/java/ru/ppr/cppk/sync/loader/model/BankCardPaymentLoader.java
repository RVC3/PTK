package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BankTransactionDao;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.sync.kpp.model.BankCardPayment;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class BankCardPaymentLoader extends BaseLoader {

    private final String loadExemptionQuery;

    public BankCardPaymentLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadExemptionQuery = buildLoadParentTicketInfoQuery();
    }

    public BankCardPayment load(long bankTransactionCashRegisterEventId) {

        String[] selectionArgs = new String[]{String.valueOf(bankTransactionCashRegisterEventId)};

        BankCardPayment bankCardPayment = null;
        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadExemptionQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                bankCardPayment = new BankCardPayment();
                bankCardPayment.BankCode = cursor.getInt(0);
                bankCardPayment.Rrn = cursor.getString(1);
                bankCardPayment.AuthCode = cursor.getString(2);
                bankCardPayment.TerminalId = cursor.getString(3);
                bankCardPayment.CardNumber = cursor.getString(4);
                bankCardPayment.CardType = cursor.getString(5);
                bankCardPayment.PaymentDateTime = new Date(cursor.getLong(6));
                bankCardPayment.Sum = new BigDecimal(cursor.getString(7));
                bankCardPayment.OrganizationId = cursor.getString(8);
                bankCardPayment.CurrencyCode = cursor.getString(9);
                bankCardPayment.SellerNumber = cursor.getString(10);
                bankCardPayment.ApplicationName = cursor.getString(11);
                bankCardPayment.CheckNumber = cursor.getString(12);
                bankCardPayment.ResponseSum = bankCardPayment.Sum;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return bankCardPayment;
    }

    private String buildLoadParentTicketInfoQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(BankTransactionDao.Properties.BankCode).append(", ");
        sb.append(BankTransactionDao.Properties.Rrn).append(", ");
        sb.append(BankTransactionDao.Properties.AuthorizationCode).append(", ");
        sb.append(BankTransactionDao.Properties.TerminalNumber).append(", ");
        sb.append(BankTransactionDao.Properties.CardPan).append(", ");
        sb.append(BankTransactionDao.Properties.CardEmitentName).append(", ");
        sb.append(BankTransactionDao.Properties.TransactionDateTime).append(", ");
        sb.append(BankTransactionDao.Properties.Total).append(", ");
        sb.append(BankTransactionDao.Properties.MerchantId).append(", ");
        sb.append(BankTransactionDao.Properties.CurrencyCode).append(", ");
        sb.append(BankTransactionDao.Properties.PointOfSaleNumber).append(", ");
        sb.append(BankTransactionDao.Properties.SmartCardApplicationName).append(", ");
        sb.append(BankTransactionDao.Properties.BankCheckNumber);
        sb.append(" FROM ");
        sb.append(BankTransactionDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}