package ru.ppr.core.dataCarrier.smartCard.passageMark.base;

/**
 * Метка прохода v.4, v.5, v.8.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkV4V5V8 extends PassageMark,
        PassageMarkWithTurnstileNumber,
        PassageMarkWithPassageTime,
        PassageMarkWithFlags {

    /**
     * Возвращает количество секунд от момента оформления ПД №1 до момента последнего прохода через турникет по ПД №1.
     *
     * @return Количество секунд от момента оформления ПД №1 до момента последнего прохода через турникет по ПД №1.
     */
    int getPd1PassageTime();

    void setPd1PassageTime(int pd1PassageTime);

    /**
     * Возвращает количество секунд от момента оформления ПД №2 до момента последнего прохода через турникет по ПД №2.
     *
     * @return Количество секунд от момента оформления ПД №2 до момента последнего прохода через турникет по ПД №2.
     */
    int getPd2PassageTime();

    void setPd2PassageTime(int pd2PassageTime);

    /**
     * Возвращает номер турникета на станции, через который был совершен проход по ПД №1.
     *
     * @return Номер турникета на станции, через который был совершен проход по ПД №1.
     */
    int getPd1TurnstileNumber();

    void setPd1TurnstileNumber(int pd1TurnstileNumber);

    /**
     * Возвращает номер турникета на станции, через который был совершен проход по ПД №2.
     *
     * @return Номер турникета на станции, через который был совершен проход по ПД №2.
     */
    int getPd2TurnstileNumber();

    void setPd2TurnstileNumber(int pd2TurnstileNumber);

    /**
     * Возвращает флаг прохода по ПД №1.
     *
     * @return Флаг прохода по ПД №1
     */
    @PassageStatus
    int getPassageStatusForPd1();

    void setPassageStatusForPd1(@PassageStatus int passageStatusForPd1);

    /**
     * Возвращает флаг прохода по ПД №2.
     *
     * @return Флаг прохода по ПД №2
     */
    @PassageStatus
    int getPassageStatusForPd2();

    void setPassageStatusForPd2(@PassageStatus int passageStatusForPd2);

    /**
     * Возвращает направление прохода по ПД №1.
     *
     * @return Направление прохода по ПД №1
     */
    @PassageType
    int getPassageTypeForPd1();

    void setPassageTypeForPd1(@PassageType int passageTypeForPd1);

    /**
     * Возвращает направление прохода по ПД №2.
     *
     * @return Направление прохода по ПД №2
     */
    @PassageType
    int getPassageTypeForPd2();

    void setPassageTypeForPd2(@PassageType int passageTypeForPd2);
}
