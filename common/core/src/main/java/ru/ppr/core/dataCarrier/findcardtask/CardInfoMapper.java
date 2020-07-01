package ru.ppr.core.dataCarrier.findcardtask;

import ru.ppr.rfid.CardData;
import ru.ppr.rfid.MifareCardType;

/**
 * Конвертер {@link CardData} в {@link CardInfo}
 *
 * @author Aleksandr Brazhkin
 */
public class CardInfoMapper {

    /**
     * Конвертирует {@link CardData} в {@link CardInfo}
     *
     * @param cardData Информация о карте с нижнего уровня
     * @return Информация о карте
     */
    public static CardInfo map(CardData cardData) {
        CardInfo cardInfo = new CardInfo();
        cardInfo.setCardUid(cardData.getCardUID());
        if (cardData.isEv1()) {
            // На всякий случай, ввиду неуверенности.
            cardInfo.setMifareCardType(MifareCardType.UltralightEV1);
        } else {
            cardInfo.setMifareCardType(cardData.getMifareCardType());
        }
        return cardInfo;
    }
}
