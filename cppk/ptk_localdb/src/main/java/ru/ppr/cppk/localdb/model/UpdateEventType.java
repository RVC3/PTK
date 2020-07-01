package ru.ppr.cppk.localdb.model;

import android.support.annotation.Nullable;

/**
 * Тип события обновления.
 */
public enum UpdateEventType {
    /**
     * Обновление ПО
     */
    SW(1, "ПО"),
    /**
     * Обновление базы НСИ
     */
    NSI(2, "НСИ"),
    /**
     * Обновление базы безопасности
     */
    SECURITY(3, "База безопасности"),
    /**
     * Обновление стоп-листов
     */
    STOP_LISTS(4, "Стоп листы"),
    /**
     * Полная Синхронизация
     */
    ALL(5, "Полная Синхронизация");

    /**
     * Код
     */
    private int code;
    /**
     * Описание
     */
    private String description;

    UpdateEventType(int code, String description) {
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
    public static UpdateEventType valueOf(int code) {
        for (UpdateEventType type : UpdateEventType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "UpdateSubject{" +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
