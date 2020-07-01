package ru.ppr.chit.domain.model.nsi;

import android.support.annotation.Nullable;

/**
 * Тип носителя ПД.
 *
 * @author Aleksandr Brazhkin
 */
public enum TicketStorageType {

    /**
     * Неизвестен
     */
    UNKNOWN(0),
    /**
     * Бумажный
     * Термолента для кассовых аппаратов 44 мм
     */
    PAPER(1),
    /**
     * СКМ
     * Социальная карта москвича
     */
    SKM(2),
    /**
     * СКМО
     * Социальная карта жителя Московской области
     */
    SKMO(6),
    /**
     * ИПК
     * Студенческая карта
     */
    IPK(7),
    /**
     * ЭТТ
     * Электронное транспортное требование
     */
    ETT(8),
    /**
     * Тройка
     * Электронная карта «Тройка»
     */
    TRK(10),
    /**
     * БСК на период
     * Бесконтактная смарт-карта без счетчика), выпускаемая ЦППК
     */
    CPPK(11),
    /**
     * БСК на количество поездок
     * Бесконтактная смарт-карта со счетчиком), выпускаемая ЦППК
     */
    CPPK_COUNTER(12),
    /**
     * БСК провожающего
     */
    SEE_OF_CARD(13),
    /**
     * Служебная карта
     * Карта для авторизации.
     */
    SERVICE(16),
    /**
     * Стрелка
     */
    STRELKA(17);

    private final int code;

    TicketStorageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static TicketStorageType valueOf(int code) {
        for (TicketStorageType ticketStorageType : TicketStorageType.values()) {
            if (ticketStorageType.getCode() == code) {
                return ticketStorageType;
            }
        }
        return null;
    }
}
