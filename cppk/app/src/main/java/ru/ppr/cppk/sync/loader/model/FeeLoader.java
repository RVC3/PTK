package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.FeeDao;
import ru.ppr.cppk.sync.kpp.model.Fee;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class FeeLoader extends BaseLoader {

    private final String loadQuery;

    public FeeLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadQuery = buildLoadQuery();
    }

    static class Columns {
        static final Column TOTAL = new Column(0, FeeDao.Properties.Total);
        static final Column NDS = new Column(1, FeeDao.Properties.Nds);
        static final Column FEE_TYPE = new Column(2, FeeDao.Properties.FeeType);

        public static Column[] all = new Column[]{
                TOTAL,
                NDS,
                FEE_TYPE
        };
    }

    private Fee load(Cursor cursor, Offset offset) {
        Fee fee = new Fee();
        fee.Total = new BigDecimal(cursor.getString(offset.value + Columns.TOTAL.index));
        fee.Nds = new BigDecimal(cursor.getString(offset.value + Columns.NDS.index));
        fee.FeeType = cursor.getInt(offset.value + Columns.FEE_TYPE.index);
        // В будущем переделать когда в Tax начнет приходить процент а не сумма
        fee.NdsPercent = getFeeVatRate(fee.Total, fee.Nds);
        offset.value += Columns.all.length;
        return fee;
    }

    private int getFeeVatRate(BigDecimal total, BigDecimal nds) {
        BigDecimal vatRateInPercents = Decimals.getVATRateIncludedFromValue(total, nds, Decimals.RoundMode.WITHOUT);
        //нужно вернуть int
        return vatRateInPercents.setScale(2, RoundingMode.HALF_UP).intValue();
    }

    public Fee load(long feeId) {

        String[] selectionArgs = new String[]{String.valueOf(feeId)};

        Fee fee = null;
        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                fee = load(cursor, new Offset());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return fee;
    }

    private String buildLoadQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(createColumnsForSelect(FeeDao.TABLE_NAME, Columns.all));
        sb.append(" FROM ");
        sb.append(FeeDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}