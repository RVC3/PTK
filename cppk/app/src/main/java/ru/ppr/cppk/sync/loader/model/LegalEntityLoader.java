package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.LegalEntityDao;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.kpp.model.LegalEntity;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class LegalEntityLoader extends BaseLoader {

    public LegalEntityLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
    }

    public static class Columns {
        static final Column CODE = new Column(0, LegalEntityDao.Properties.Code);
        static final Column INN = new Column(1, LegalEntityDao.Properties.INN);
        static final Column NAME = new Column(2, LegalEntityDao.Properties.Name);

        public static Column[] all = new Column[]{
                CODE,
                INN,
                NAME
        };
    }

    public LegalEntity load(Cursor cursor, Offset offset) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.Code = cursor.getString(offset.value + Columns.CODE.index);
        legalEntity.INN = cursor.getString(offset.value + Columns.INN.index);
        legalEntity.Name = cursor.getString(offset.value + Columns.NAME.index);
        offset.value += Columns.all.length;
        return legalEntity;
    }
}