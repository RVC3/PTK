package ru.ppr.chit.securitydb.daosession;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import java.util.Map;

import ru.ppr.chit.securitydb.greendao.DaoSession;
import ru.ppr.database.greendao.GreenDaoDatabase;

/**
 * Обертка над {@link DaoSession} для обеспечения возможности получения {@link GreenDaoDatabase}.
 *
 * @author Aleksandr Brazhkin
 */
public class SecurityDaoSession extends DaoSession {

    private final GreenDaoDatabase db;

    SecurityDaoSession(GreenDaoDatabase db, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig> daoConfigMap) {
        super(db, IdentityScopeType.Session, daoConfigMap);
        this.db = db;
    }

    @Override
    public GreenDaoDatabase getDatabase() {
        return db;
    }
}
