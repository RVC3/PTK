package ru.ppr.chit.nsidb.daosession;

import ru.ppr.chit.nsidb.greendao.DaoMaster;
import ru.ppr.database.greendao.GreenDaoDatabase;

/**
 * Обертка над {@link DaoMaster} для обеспечения возможности получения {@link NsiDaoSession}.
 *
 * @author Aleksandr Brazhkin
 */
public class NsiDaoMaster extends DaoMaster {

    private final GreenDaoDatabase db;

    public NsiDaoMaster(GreenDaoDatabase db) {
        super(db);
        this.db = db;
    }

    @Override
    public NsiDaoSession newSession() {
        return new NsiDaoSession(db, daoConfigMap);
    }
}
