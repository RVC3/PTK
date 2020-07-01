package ru.ppr.chit.localdb.daosession;

import ru.ppr.chit.localdb.greendao.DaoMaster;
import ru.ppr.database.greendao.GreenDaoDatabase;

/**
 * Обертка над {@link DaoMaster} для обеспечения возможности получения {@link LocalDaoSession}.
 *
 * @author Aleksandr Brazhkin
 */
public class LocalDaoMaster extends DaoMaster {

    private final GreenDaoDatabase db;

    public LocalDaoMaster(GreenDaoDatabase db) {
        super(db);
        this.db = db;
    }

    @Override
    public LocalDaoSession newSession() {
        return new LocalDaoSession(db, daoConfigMap);
    }
}
