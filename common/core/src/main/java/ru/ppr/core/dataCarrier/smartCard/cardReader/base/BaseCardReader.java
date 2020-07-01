package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.rfid.IRfid;

/**
 * Базовый класс для ридеров смарт-карт.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseCardReader implements CardReader {
    /**
     * Считываель RFID
     */
    protected final IRfid rfid;
    /**
     * Информация о смарт-карте
     */
    protected final CardInfo cardInfo;

    public BaseCardReader(IRfid rfid,
                          CardInfo cardInfo) {
        this.rfid = rfid;
        this.cardInfo = cardInfo;
    }

    @Override
    public CardInfo getCardInfo() {
        return cardInfo;
    }

    /**
     * Вырезает из масива байтов кусок
     *
     * @param data       массив байтов
     * @param startIndex индекс, с которого необходимо вырезать масив байтов
     * @param countByte  длина куска, в байтах
     * @return
     */
    protected byte[] getBytesFromData(byte[] data, int startIndex, int countByte) {
        byte[] tmpData = new byte[countByte];
        System.arraycopy(data, startIndex, tmpData, 0, countByte);
        return tmpData;
    }
}
