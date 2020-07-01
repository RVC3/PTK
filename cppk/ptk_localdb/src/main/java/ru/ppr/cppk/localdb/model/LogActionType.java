package ru.ppr.cppk.localdb.model;

import android.support.annotation.Nullable;

/**
 * Тип действия для {@link LogEvent}.
 */
public enum LogActionType {

    APP_CRASH(1, LogMessageType.ERROR, "Сбой в работе приложения"),

    STANDARD_MODE_ON(2, LogMessageType.INFO, "Переход ПО в штатный режим"),

    STANDARD_MODE_OFF(3, LogMessageType.INFO, "Выход ПО из штатного режима"),

    SERVICE_MODE_ON(4, LogMessageType.INFO, "Переход ПО в служебный режим"),

    SERVICE_MODE_OFF(5, LogMessageType.INFO, "Выход ПО из служебного режима"),

    EMERGENCY_MODE_ON(6, LogMessageType.INFO, "Переход ПО в аварийный режим"),

    EMERGENCY_MODE_OFF(7, LogMessageType.INFO, "Выход ПО из аварийного режима"),

    APP_START(8, LogMessageType.INFO, "Штатное включение ПО"),

    APP_END(9, LogMessageType.INFO, "Штатное выключение ПО"),

    AUTH_SUCCESS(10, LogMessageType.INFO, "Успешная авторизация пользователя"),

    AUTH_ERROR(11, LogMessageType.WARNING, "Неудачная авторизация пользователя"),

    DEVICE_IS_NOT_CONNECTED(12, LogMessageType.ERROR, "Ошибка подключения к внешнему устройству"),

    ATTEMPT_TO_CHANGE_TIME(13, LogMessageType.WARNING, "Попытка изменить кассиром-контролером время в ПО ПТК более чем на 5 минут"),

    TIME_CHANGE(14, LogMessageType.INFO, "Изменение времени в ПО ПТК"),

    SYNCHRONISATION_WITH_ARM_START(15, LogMessageType.INFO, "Синхронизация с ARM"),

    SYNCHRONISATION_WITH_ARM_END(16, LogMessageType.INFO, "Завершение синхронизации с ARM");

    /**
     * Код действия
     */
    private int code;
    /**
     * Описание действия
     */
    private String description;

    private LogMessageType messageType;

    LogActionType(int code, LogMessageType messageType, String description) {
        this.code = code;
        this.description = description;
        this.messageType = messageType;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public LogMessageType getMessageType() {
        return messageType;
    }

    @Nullable
    public static LogActionType valueOf(int code) {
        for (LogActionType logActionType : values()) {
            if (logActionType.getCode() == code) {
                return logActionType;
            }
        }
        return null;
    }

}
