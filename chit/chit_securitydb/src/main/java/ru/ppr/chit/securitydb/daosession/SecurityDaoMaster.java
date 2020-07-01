package ru.ppr.chit.securitydb.daosession;

import ru.ppr.chit.securitydb.greendao.DaoMaster;
import ru.ppr.database.greendao.GreenDaoDatabase;

/**
 * Обертка над {@link DaoMaster} для обеспечения возможности получения {@link SecurityDaoSession}.
 *
 * @author Aleksandr Brazhkin
 */
public class SecurityDaoMaster extends DaoMaster {

    private final GreenDaoDatabase db;

    public SecurityDaoMaster(GreenDaoDatabase db) {
        super(db);
        this.db = db;
    }

    @Override
    public SecurityDaoSession newSession() {
        return new SecurityDaoSession(db, daoConfigMap);
    }
}
