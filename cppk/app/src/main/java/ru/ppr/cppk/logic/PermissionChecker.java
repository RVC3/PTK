package ru.ppr.cppk.logic;

import ru.ppr.cppk.Holder;
import ru.ppr.cppk.helpers.UserSessionInfo;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;

/**
 * Класс для проверки разрешений текущего пользователя.
 *
 * @author Aleksandr Brazhkin
 */
public class PermissionChecker {

    private UserSessionInfo mUserSessionInfo;
    private Holder<SecurityDaoSession> mSecurityDaoSession;

    public PermissionChecker(UserSessionInfo userSessionInfo, Holder<SecurityDaoSession> securityDaoSession) {
        mUserSessionInfo = userSessionInfo;
        mSecurityDaoSession = securityDaoSession;
    }

    /**
     * Выполняет проверку разрешений.
     *
     * @param permission Разрешение
     * @return {@code true}, если у пользователя есть разрешение, {@code false} - иначе
     */
    public boolean checkPermission(PermissionDvc permission) {
        RoleDvc role = mUserSessionInfo.getCurrentUser().getRole();
        return mSecurityDaoSession.get().getRolePermissionDvcDao().isPermissionEnabled(role, permission);
    }

}
