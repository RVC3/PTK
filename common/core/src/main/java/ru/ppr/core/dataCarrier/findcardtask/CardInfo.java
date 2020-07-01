package ru.ppr.core.dataCarrier.findcardtask;

import ru.ppr.rfid.MifareCardType;

/**
 * Информация о смарт карте.
 *
 * @author Aleksandr Brazhkin
 */
public class CardInfo {
    /**
     * UID карты
     */
    private byte[] cardUid = null;
    /**
     * Физический тип карты
     */
    private MifareCardType mifareCardType = MifareCardType.Unknown;

    public CardInfo() {

    }

    public byte[] getCardUid() {
        return cardUid;
    }

    public void setCardUid(byte[] cardUid) {
        this.cardUid = cardUid;
    }

    public MifareCardType getMifareCardType() {
        return mifareCardType;
    }

    public void setMifareCardType(MifareCardType mifareCardType) {
        this.mifareCardType = mifareCardType;
    }
}
