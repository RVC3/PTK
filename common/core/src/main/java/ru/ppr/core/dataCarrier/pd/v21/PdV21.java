package ru.ppr.core.dataCarrier.pd.v21;

import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;
import ru.ppr.core.dataCarrier.pd.base.PdWithoutPlace;

/**
 * ПД v.15.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdV21 extends PdWithTicketType {

    /**
     * Возвращает дату начала действия ПД.
     *
     * @return Дата начала действия ПД: количество дней с дня продажи. От 0 (в день продажи) до 31.
     */
    int getStartDayOffset();

    /**
     * Возвращает порядковый номер ПД.
     *
     * @return Порядковый номер (не фискальный) чека за календарный месяц
     */
    int getOrderNumber();

    /**
     * Возвращает идентификатор услуги.
     *
     * @return Код услуги по справочнику "Стоимости услуг"
     */
    long getServiceId();

}
