package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;

/**
 * Ридер смарт-карт.
 *
 * @author Aleksandr Brazhkin
 */
public interface CardReader {

    /**
     * Возвращает информацию по смарт-карте.
     *
     * @return Информация по смарт-карте
     */
    CardInfo getCardInfo();



}
