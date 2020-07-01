package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.CashierDao;
import ru.ppr.cppk.sync.kpp.model.Cashier;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
public class CashierLoader extends BaseLoader {

    public CashierLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
    }

    public static class Columns {
        static final Column USER_LOGIN = new Column(0, CashierDao.Properties.UserLogin);
        static final Column CASHIER_OFFICIAL_CODE = new Column(1, CashierDao.Properties.OfficialCode);
        static final Column CASHIER_FIO = new Column(2, CashierDao.Properties.Fio);

        public static Column[] all = new Column[]{
                USER_LOGIN,
                CASHIER_OFFICIAL_CODE,
                CASHIER_FIO
        };
    }

    public Cashier load(Cursor cursor, Offset offset) {
        Cashier cashier = new Cashier();
        cashier.userLogin = cursor.getString(offset.value + Columns.USER_LOGIN.index);
        cashier.officialCode = cursor.getString(offset.value + Columns.CASHIER_OFFICIAL_CODE.index);
        cashier.fio = cursor.getString(offset.value + Columns.CASHIER_FIO.index);
        offset.value += Columns.all.length;
        return cashier;
    }
}
