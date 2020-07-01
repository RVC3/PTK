package ru.ppr.core.dataCarrier.smartCard.serviceData.base;

/**
 * @author Aleksandr Brazhkin
 */
public interface ServiceDataWithValidityTime extends ServiceData {
    /**
     * Возвращает срок действия.
     * Количество дней действия начиная с даты инициализации (не более 1.5 лет), UTC
     *
     * @return Срок действия
     */
    int getValidityTime();
}
