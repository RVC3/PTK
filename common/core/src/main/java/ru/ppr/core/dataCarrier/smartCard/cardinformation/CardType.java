package ru.ppr.core.dataCarrier.smartCard.cardinformation;

import android.support.annotation.Nullable;

import java.util.EnumSet;

import ru.ppr.core.dataCarrier.smartCard.entity.BscType;

/**
 * Тип смарт-карты, определяемый на основании данных из информационного блоке (2-ой сектор, 0-ой блок, 4-ый байт).
 *
 * @author Aleksandr Brazhkin
 */
public enum CardType {
    /**
     * СКМ
     * Социальная карта москвича
     */
    SKM(2, BscType.SKM_SKMO_IPK),
    /**
     * СКМО
     * Социальная карта жителя Московской области
     */
    SKMO(6, BscType.SKM_SKMO_IPK),
    /**
     * ИПК
     * Студенческая карта
     */
    IPK(7, BscType.SKM_SKMO_IPK),
    /**
     * ЭТТ
     * Электронное транспортное требование
     */
    ETT(8, BscType.ETT),
    /**
     * Тройка
     * Электронная карта «Тройка»
     */
    TRK(10, BscType.TROIKA, BscType.TROIKA_STRELKA),
    /**
     * БСК на период
     * Бесконтактная смарт-карта без счетчика), выпускаемая ЦППК
     */
    CPPK(11, BscType.CPPK, BscType.CPPK_PLUS),
    /**
     * БСК на количество поездок
     * Бесконтактная смарт-карта со счетчиком), выпускаемая ЦППК
     */
    CPPK_COUNTER(12, BscType.CPPK_COUNTER, BscType.EV_1_CPPK_COUNTER),
    /**
     * БСК провожающего
     */
    SEE_OF_CARD(13, BscType.SEE_OFF),
    /**
     * Служебная карта
     * Карта для авторизации.
     */
    SERVICE(16, BscType.SERVICE, BscType.SERVICE_26),
    /**
     * Стрелка
     */
    STRELKA(17, BscType.STRELKA, BscType.STRELKA_TROIKA, BscType.STRELKA_TROIKA_VOLD),
    /**
     * Льготные СКМ, СКМО или ИПК
     */
    SCM_SCMO_or_IPK_BSC(-1, BscType.SKM_SKMO_IPK);

    /**
     * Код типа карты по НСИ
     */
    private final int nsiCode;
    /**
     * Числовой коды smart-карты
     */
    private EnumSet<BscType> bscTypes;

    CardType(int nsiCode, BscType first, BscType... rest) {
        this.nsiCode = nsiCode;
        this.bscTypes = EnumSet.of(first, rest);
    }

    public int getNsiCode() {
        return nsiCode;
    }

    @Nullable
    public static CardType valueOf(int nsiCode) {
        for (CardType cardType : CardType.values()) {
            if (cardType.getNsiCode() == nsiCode) {
                return cardType;
            }
        }
        return null;
    }

    /**
     * Маппит коды записанные на карте в коды фигурирующие в БД, смотри файл
     * ТСОППД.Структура ПД.2014.11.05 и класс SmartCardType
     */
    @Nullable
    public static CardType valueOf(BscType bscType) {
        if (bscType == BscType.SKM_SKMO_IPK) {
            return SCM_SCMO_or_IPK_BSC;
        }
        for (CardType cardType : CardType.values()) {
            if (cardType.bscTypes.contains(bscType)) {
                return cardType;
            }
        }
        return null;
    }
}
