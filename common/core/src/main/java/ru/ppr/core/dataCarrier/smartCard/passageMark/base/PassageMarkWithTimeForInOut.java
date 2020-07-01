package ru.ppr.core.dataCarrier.smartCard.passageMark.base;

/**
 * Метка прохода со временем последнего входа/выхода.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkWithTimeForInOut extends PassageMark {

    /**
     * Возвращает количество секунд от момента оформления услуги до момента входа через турникет.
     *
     * @return Количество секунд от момента оформления услуги до момента входа через турникет.
     */
    int getInPassageTime();

    /**
     * Возвращает количество секунд от момента оформления услуги до момента выхода через турникет.
     *
     * @return Количество секунд от момента оформления услуги до момента выхода через турникет.
     */
    int getOutPassageTime();
}
