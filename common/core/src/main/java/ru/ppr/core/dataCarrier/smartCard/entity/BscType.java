package ru.ppr.core.dataCarrier.smartCard.entity;

/**
 * Тип БСК.
 *
 * @author Aleksandr Brazhkin
 */
public enum BscType {

    /**
     * Неизвестный
     */
    UNKNOWN(0x00),
    /**
     * Общая метка для СКМ и ИПК карт
     */
    SKM_SKMO_IPK(0x30),
    /**
     * обычная ЦППКшная метка
     */
    CPPK(0x21),
    /**
     * Метка зеленой карты
     */
    CPPK_PLUS(0x28),
    SEE_OFF(0x43),
    CPPK_COUNTER(0x33),
    SERVICE(0x77),
    SERVICE_26(0x26),
    TROIKA(0x23),
    ETT(0x24),
    STRELKA(0x27),
    EV_1_CPPK_COUNTER(0x35),
    //CPPKPP-30884
    TROIKA_STRELKA(0x45), //считаем ее тройкой
    STRELKA_TROIKA(0x2E), //считаем ее стрелкой
    STRELKA_TROIKA_VOLD(0x46); //считаем ее стрелкой

    private final byte rawCode;

    BscType(int rawCode) {
        this.rawCode = (byte) rawCode;
    }

    public static BscType getByRawCode(byte rawCode) {
        for (BscType bscType : values()) {
            if (bscType.rawCode == rawCode) {
                return bscType;
            }
        }
        return UNKNOWN;
    }

}
