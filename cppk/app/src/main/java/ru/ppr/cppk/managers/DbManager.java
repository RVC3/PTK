package ru.ppr.cppk.managers;

import ru.ppr.cppk.Holder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.managers.db.LocalDbManager;
import ru.ppr.cppk.managers.db.NsiDbManager;
import ru.ppr.cppk.managers.db.SecurityDbManager;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.security.SecurityDaoSession;

/**
 * Менеджер работы с БД.
 * @deprecated Use {@link LocalDbManager}, {@link NsiDbManager},{@link SecurityDbManager} instead
 *
 * @author Aleksandr Brazhkin
 */
@Deprecated
public class DbManager {

    private static final String TAG = Logger.makeLogTag(DbManager.class);

    private final Holder<LocalDaoSession> localDaoSession;
    private final Holder<NsiDaoSession> nsiDaoSession;
    private final Holder<SecurityDaoSession> securityDaoSession;

    public DbManager(Holder<LocalDaoSession> localDaoSession, Holder<NsiDaoSession> nsiDaoSession, Holder<SecurityDaoSession> securityDaoSession) {
        this.localDaoSession = localDaoSession;
        this.nsiDaoSession = nsiDaoSession;
        this.securityDaoSession = securityDaoSession;
    }

    public Holder<LocalDaoSession> getLocalDaoSession() {
        return localDaoSession;
    }

    public Holder<NsiDaoSession> getNsiDaoSession() {
        return nsiDaoSession;
    }

    public Holder<SecurityDaoSession> getSecurityDaoSession() {
        return securityDaoSession;
    }
}
