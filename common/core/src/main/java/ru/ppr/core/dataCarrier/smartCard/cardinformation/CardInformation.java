package ru.ppr.core.dataCarrier.smartCard.cardinformation;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.rfid.MifareCardType;
import ru.ppr.utils.CommonUtils;

/**
 * @author Aleksandr Brazhkin
 */
public abstract class CardInformation {

    private final CardInfo cardInfo;

    CardInformation(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
    }

    public abstract CardType getCardType();

    public abstract String getOuterNumberAsString();

    public abstract String getOuterNumberAsFormattedString();

    public String getCrystalSerialNumberAsString() {
        return String.valueOf(CommonUtils.convertByteToLong(getCardUid(), ByteOrder.LITTLE_ENDIAN));
    }

    public abstract Date getExpiryDate();

    public byte[] getCardUid() {
        return cardInfo.getCardUid();
    }

    public MifareCardType getMifareCardType() {
        return cardInfo.getMifareCardType();
    }

    @Override
    public String toString() {
        return "CardInformation{" +
                "cardInfo=" + cardInfo +
                "CardType=" + getCardType() +
                '}';
    }
}
