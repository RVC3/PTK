package ru.ppr.chit.nsidb.daosession;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import java.util.Map;

import ru.ppr.chit.nsidb.greendao.DaoSession;
import ru.ppr.database.greendao.GreenDaoDatabase;

/**
 * Обертка над {@link DaoSession} для обеспечения возможности получения {@link GreenDaoDatabase}.
 *
 * @author Aleksandr Brazhkin
 */
public class NsiDaoSession extends DaoSession {

    private final GreenDaoDatabase db;

    public NsiDaoSession(GreenDaoDatabase db, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig> daoConfigMap) {
        super(db, IdentityScopeType.Session, daoConfigMap);
        this.db = db;
    }

    @Override
    public GreenDaoDatabase getDatabase() {
        return db;
    }
}
