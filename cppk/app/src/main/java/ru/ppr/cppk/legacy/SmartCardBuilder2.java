package ru.ppr.cppk.legacy;

import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * Билдер для {@link SmartCard}.
 *
 * @author Aleksandr Brazhkin
 */
public class SmartCardBuilder2 {

    private CardInformation cardInformation;

    public SmartCardBuilder2() {

    }

    public SmartCardBuilder2 setBscInformation(CardInformation cardInformation) {
        this.cardInformation = cardInformation;
        return this;
    }

    public SmartCard build() {
        SmartCard card = new SmartCard();
        try {
            TicketStorageType ticketStorageType = new CardTypeToTicketStorageTypeMapper().map(cardInformation.getCardType());
            card.setType(ticketStorageType);
            card.setOuterNumber(cardInformation.getOuterNumberAsFormattedString().replace(" ", "")); // уберем пробел
            card.setCrystalSerialNumber(cardInformation.getCrystalSerialNumberAsString());
            card.setUsageCount(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return card;
    }
}
