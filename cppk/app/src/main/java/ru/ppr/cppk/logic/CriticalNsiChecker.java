package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.helpers.UserSessionInfo;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;

/**
 * @author Dmitry Nevolin
 */
public class CriticalNsiChecker {

    private static final String TAG = Logger.makeLogTag(CriticalNsiChecker.class);

    private final NsiVersionManager nsiVersionManager;
    private final UserSessionInfo userSessionInfo;
    private final PermissionChecker permissionChecker;

    public CriticalNsiChecker(@NonNull NsiVersionManager nsiVersionManager,
                              @NonNull UserSessionInfo userSessionInfo,
                              @NonNull PermissionChecker permissionChecker) {
        this.nsiVersionManager = nsiVersionManager;
        this.userSessionInfo = userSessionInfo;
        this.permissionChecker = permissionChecker;
    }

    public boolean checkCriticalNsiCloseDialogShouldBeShown() {
        Logger.trace(TAG, "checkCriticalNsiCloseDialogShouldBeShown() START");

        Date criticalNsiChangeDate = nsiVersionManager.getCriticalNsiChangeDate();

        if (criticalNsiChangeDate != null) {
            Date currentDate = new Date();

            if (currentDate.after(criticalNsiChangeDate)) {
                Logger.trace(TAG, "checkCriticalNsiCloseDialogShouldBeShown() END = true");

                return true;
            }
        }

        Logger.trace(TAG, "checkCriticalNsiCloseDialogShouldBeShown() END = false");

        return false;
    }

    public boolean checkCriticalNsiCloseShiftPermissions() {
        Logger.trace(TAG, "checkCriticalNsiCloseShiftPermissions() START");

        if (userSessionInfo.getCurrentUser() == null || userSessionInfo.getCurrentUser().getRole() == null) {
            Logger.trace(TAG, "checkCriticalNsiCloseShiftPermissions() END = false " +
                    "(userSessionInfo.getCurrentUser() == null (" + (userSessionInfo.getCurrentUser() == null) +
                    ") || userSessionInfo.getCurrentUser().getRole() == null (" + (userSessionInfo.getCurrentUser().getRole() == null) + "))");

            return false;
        }

        RoleDvc role = userSessionInfo.getCurrentUser().getRole();

        // Не показываем если пользователь - рут, либо если у пользователя нет разрешения,
        // пропустим https://aj.srvdev.ru/browse/CPPKPP-28017
        boolean isRoot = role.isRoot();
        boolean hasPermissions = permissionChecker.checkPermission(PermissionDvc.CloseShift);
        if (isRoot || !hasPermissions) {
            Logger.trace(TAG, "checkCriticalNsiCloseShiftPermissions() END = false " +
                    "(isRoot (" + role.isRoot() + ") || !hasPermissions (" + !hasPermissions + "))");

            return false;
        }

        Logger.trace(TAG, "checkCriticalNsiCloseShiftPermissions() END = false");

        return true;
    }

}
