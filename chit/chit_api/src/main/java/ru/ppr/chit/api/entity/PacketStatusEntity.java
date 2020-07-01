package ru.ppr.chit.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Статус сборки пакета
 *
 * @author Dmitry Nevolin
 */
public enum PacketStatusEntity {

    /**
     * Ошибка сборки пакета
     */
    @SerializedName("0")
    ERROR(0),
    /**
     * Идет процесс сборки пакета
     */
    @SerializedName("1")
    PROCESS(1),
    /**
     * Пакет готов
     */
    @SerializedName("2")
    READY(2),

    /**
     * Ожидание данных от внешней системы
     */
    @SerializedName("3")
    PENDING_DATA(3);

    private final int code;

    PacketStatusEntity(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
