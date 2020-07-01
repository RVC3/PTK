package ru.ppr.cppk.managers.db;

import ru.ppr.cppk.Holder;
import ru.ppr.security.SecurityDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
public class SecurityDaoSessionUpdatesListener {

    public SecurityDaoSessionUpdatesListener(SecurityDbManager securityDbManager,
                                             Holder<SecurityDaoSession> securityDaoSession) {
        securityDbManager
                .addDaoSessionResetListener(daoSession -> {
                    securityDaoSession.set(daoSession);
                });
    }
}
