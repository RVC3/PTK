package ru.ppr.cppk.legacy;

import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.entity.event.model.SmartCard;

/**
 * Билдер для {@link SmartCard}.
 *
 * @author Aleksandr Brazhkin
 */
public class SmartCardBuilder {

    private BscInformation bscInformation;

    public SmartCardBuilder() {

    }

    public SmartCardBuilder setBscInformation(BscInformation bscInformation) {
        this.bscInformation = bscInformation;
        return this;
    }

    public SmartCard build() {
        SmartCard card = new SmartCard();
        try {
            card.setType(bscInformation.getSmartCardTypeBsc());
            card.setOuterNumber(bscInformation.getFormattedOuterNumber().replace(" ", "")); // уберем пробел
            card.setCrystalSerialNumber(bscInformation.getCrustalSerialNumberString());
            card.setUsageCount(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return card;
    }
}
