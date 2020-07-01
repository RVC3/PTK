package ru.ppr.cppk.db.local.query;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CouponReadEventDao;
import ru.ppr.cppk.db.local.CppkTicketReturnDao;
import ru.ppr.cppk.db.local.CppkTicketSaleDao;
import ru.ppr.database.QueryBuilder;

/**
 * Запрос на наличие неаннулированной продажи ПД по номеру талона ТППД.
 *
 * @author Aleksandr Brazhkin
 */
public class SaleWithCouponNumberQuery extends BaseLocalQuery {

    private final long couponNumber;

    public SaleWithCouponNumberQuery(LocalDaoSession localDaoSession, long couponNumber) {
        super(localDaoSession);
        this.couponNumber = couponNumber;
    }

    /**
     * Выполняет запрос.
     *
     * @return {@code true} если продажа существует, {@code false} иначе
     */
    public boolean query() {
        QueryBuilder qb = new QueryBuilder();
        qb.select().arg(1).from(CppkTicketSaleDao.TABLE_NAME);
        qb.where().exists(() -> {
            qb.selectAll().from(CouponReadEventDao.TABLE_NAME);
            qb.where();
            qb.field(CppkTicketSaleDao.TABLE_NAME, CppkTicketSaleDao.Properties.CouponReadEventId);
            qb.eq();
            qb.field(CouponReadEventDao.TABLE_NAME, BaseEntityDao.Properties.Id);
            qb.and();
            qb.field(CouponReadEventDao.TABLE_NAME, CouponReadEventDao.Properties.PreTicketNumber).eq(couponNumber);
        });
        qb.and().notExists(() -> {
            qb.selectAll().from(CppkTicketReturnDao.TABLE_NAME);
            qb.where();
            qb.field(CppkTicketSaleDao.TABLE_NAME, BaseEntityDao.Properties.Id);
            qb.eq();
            qb.field(CppkTicketReturnDao.TABLE_NAME, CppkTicketReturnDao.Properties.CppkTicketSaleId);
        });
        qb.limit(1);

        Cursor cursor = null;
        try {
            cursor = qb.build().run(db());
            return cursor.moveToNext();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
