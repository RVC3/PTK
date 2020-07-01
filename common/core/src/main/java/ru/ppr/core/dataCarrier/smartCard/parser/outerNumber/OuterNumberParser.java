package ru.ppr.core.dataCarrier.smartCard.parser.outerNumber;

import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;

/**
 * Парсер внешнего номера карты для карт ЦППК на период, Тройка, Стрелка.
 *
 * @author Aleksandr Brazhkin
 */
public interface OuterNumberParser {

    /**
     * Парсит внешний номер карты.
     *
     * @param data Данные ПД
     * @return Внешний номер
     */
    OuterNumber parse(byte[] data);

}
