package ru.ppr.core.dataCarrier.smartCard.serviceData.base;

/**
 * @author Aleksandr Brazhkin
 */
public interface ServiceDataWithOrderNumber extends ServiceData {

    /**
     * Возвращает порядковый номер.
     * Порядковый номер СТУ, назначается на АРМ Карт доступа, является частью уникального номера СТУ, может обнуляться.
     *
     * @return Порядковый номер
     */
    int getOrderNumber();
}
