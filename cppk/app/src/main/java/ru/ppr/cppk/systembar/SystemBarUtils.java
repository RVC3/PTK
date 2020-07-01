package ru.ppr.cppk.systembar;

import android.app.Activity;

import ru.ppr.cppk.EnterPinActivity;
import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.SplashActivity;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.export.ServiceUtils;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.ui.activity.ArmConnectedStateActivity;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.RoleDvc;

public class SystemBarUtils {

    /**
     * Функция проверяет содержит ли массив строк такую строку
     *
     * @param array
     * @param string
     * @return
     */
    private static boolean isContain(String[] array, String string) {
        for (String item : array) {
            if (item.equals(string))
                return true;
        }
        return false;
    }

    /**
     * Проверка на возможность существования данного окна
     *
     * @return
     */
    public static boolean isValid(Activity activity) {

        String name = activity.getClass().getName();

        if (!Globals.getInstance().isAllInited()) {
            if (!isContain(GlobalConstants.notAllInitedActivitysEnabled, name)) {
                Logger.error(SystemBarUtils.class, "завершение активити из-за флага isAllInited=false " + name);
                return false;
            }
        }

        // если по каким-то причинам текущий юзер не определен - выкидываем на сплаш
        RoleDvc role = Di.INSTANCE.getUserSessionInfo().getCurrentUser().getRole();
        if (role == null || role.getId() == -1) {
            if (!isContain(GlobalConstants.defaultUserIdActivitysEnabled, name)) {
                Logger.error(SystemBarUtils.class, "завершение активити из-за неизвестного ид роли " + name);
                return false;
            }
        }

        if (Globals.getInstance().getPrivateSettingsHolder().get().getTerminalNumber() <= 0) {
            if (!isContain(GlobalConstants.defaultPtkNumberActivitysEnabled, name)) {
                Logger.error(SystemBarUtils.class, "завершение активити из-за незаданного номера ПТК " + name);
                return false;
            }
        }
        if (SharedPreferencesUtils.getSerialNumber(activity.getApplicationContext()) == null) {
            if (!isContain(GlobalConstants.defaultSerialNumberActivitysEnabled, name)) {
                Logger.error(SystemBarUtils.class, "завершение активити из-за незаданного серийного номера оборудования " + name);
                return false;
            }
        }
        return true;
    }

    /**
     * Проверяет является ли эта Activity окном синхронизации с АРМ
     */
    public static boolean isArmConnectedStateActivity(Activity activity) {
        return activity.getClass().getName().equals(ArmConnectedStateActivity.class.getName());
    }

    /**
     * Проверяет является ли эта Activity Окном ввода пароля
     */
    public static boolean isEnterPinActivity(Activity activity) {
        return activity.getClass().getName().equals(EnterPinActivity.class.getName());
    }

    /**
     * Проверяет можно ли запускать сервис синхронизации с АРМ с этой Activity
     */
    private static boolean isArmServiceMustBeRunning(Activity activity) {
        String name = activity.getClass().getName();
        if (!isContain(GlobalConstants.notRunningArmServiceActivitysEnabled, name)) {
            Logger.trace("SystemBarUtils", "Сервис синхронизации с АРМ должен быть запущен в этом классе: " + name);
            return true;
        }
        return false;
    }

    /**
     * Проверяет является ли текущая Activity SplashActivity
     */
    public static boolean isSplash(Activity activity) {
        String name = activity.getClass().getName();
        return name.equals(SplashActivity.class.getName());
    }

    /**
     * Если это необходимо - запускает сервис синхронизации с АРМ
     */
    public static void checkAndStartArmService(Activity activity) {
        if (SystemBarUtils.isArmServiceMustBeRunning(activity) && !ServiceUtils.isArmServiceRunning(activity)) {
            Logger.trace("SystemBarUtils", "---Проверка показала, что нужно запустить сервис синхронизации с АРМ " + activity.getClass().getSimpleName());
            ServiceUtils.get().registerPowerDetect(activity.getClass().getSimpleName());
        } else {
            Logger.trace("SystemBarUtils", "---Проверка показала, что не требуется запуск сервиса синхронизации с АРМ " + activity.getClass().getSimpleName());
        }
    }

}
