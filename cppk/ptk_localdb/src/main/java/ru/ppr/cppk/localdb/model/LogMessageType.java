package ru.ppr.cppk.localdb.model;

import android.support.annotation.Nullable;

/**
 * Тип сообщения для {@link LogEvent}.
 */
public enum LogMessageType {

    INFO(0, "Сообщение"),

    WARNING(1, "Предупреждение"),

    ERROR(2, "Ошибка");

    /**
     * Код сообщения
     */
    private int code;
    /**
     * Описание сообщения
     */
    private String description;

    LogMessageType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Nullable
    public static LogMessageType valueOf(int code) {
        for (LogMessageType logMessageType : values()) {
            if (logMessageType.getCode() == code) {
                return logMessageType;
            }
        }
        return null;
    }

}
