package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.CashRegisterDao;
import ru.ppr.cppk.sync.kpp.model.CashRegister;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
public class CashRegisterLoader extends BaseLoader {

    public CashRegisterLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
    }

    public static class Columns {
        static final Column MODEL = new Column(0, CashRegisterDao.Properties.Model);
        static final Column SERIAL_NUMBER = new Column(1, CashRegisterDao.Properties.SerialNumber);
        static final Column INN = new Column(2, CashRegisterDao.Properties.Inn);
        static final Column EKLZ_NUMBER = new Column(3, CashRegisterDao.Properties.EklzNumber);
        static final Column FN_SERIAL = new Column(4, CashRegisterDao.Properties.FnSerial);

        public static Column[] all = new Column[]{
                MODEL,
                SERIAL_NUMBER,
                INN,
                EKLZ_NUMBER,
                FN_SERIAL
        };
    }

    public CashRegister load(Cursor cursor, Offset offset) {
        CashRegister cashRegister = new CashRegister();
        cashRegister.model = cursor.getString(offset.value + Columns.MODEL.index);
        cashRegister.serialNumber = cursor.getString(offset.value + Columns.SERIAL_NUMBER.index);
        cashRegister.inn = cursor.getString(offset.value + Columns.INN.index);
        cashRegister.eklzNumber = cursor.getString(offset.value + Columns.EKLZ_NUMBER.index);
        cashRegister.fnSerial = cursor.getString(offset.value + Columns.FN_SERIAL.index);
        offset.value += Columns.all.length;
        return cashRegister;
    }
}
