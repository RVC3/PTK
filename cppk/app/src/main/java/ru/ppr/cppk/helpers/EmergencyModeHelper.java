package ru.ppr.cppk.helpers;

import ru.ppr.cppk.Globals;

/**
 * Created by Александр on 16.05.2016.
 */
public class EmergencyModeHelper {

    /**
     * Для реакции на необработанные исключения
     * @param throwable исключение
     */
    public static void startEmergencyMode(Throwable throwable) {
        Globals.getInstance().getCrashReporter().uncaughtException(Thread.currentThread(), throwable);
    }

    /**
     * Для прямого перехода в аварийный режим (например, по нажатию на кнопку)
     */
    public static void startEmergencyModeDirectly() {
        startEmergencyMode(new Throwable("Direct navigation to emergency mode"));
    }
}
