package ru.ppr.cppk.dataCarrier;

import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumberWithBindingStatus;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardType;
import ru.ppr.cppk.legacy.CardTypeToTicketStorageTypeMapper;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * @author Aleksandr Brazhkin
 */
public class OuterNumberMapper {

    public OuterNumberMapper() {

    }

    public ru.ppr.cppk.dataCarrier.entity.BscInformation toLegacyBscInformation(OuterNumber outerNumber, byte[] cardUid) {
        ru.ppr.cppk.dataCarrier.entity.BscInformation legacyBscInformation = new ru.ppr.cppk.dataCarrier.entity.BscInformation();
        legacyBscInformation.setCardUID(cardUid);

        CardType cardType = CardType.valueOf(outerNumber.getBscType());
        TicketStorageType ticketStorageType = new CardTypeToTicketStorageTypeMapper().map(cardType);
        legacyBscInformation.setTypeBsc(ticketStorageType);
        legacyBscInformation.setValidityTime(outerNumber.getValidityTerm());
        legacyBscInformation.setInitDate(outerNumber.getInitDate());
        legacyBscInformation.setExemptionCode(0);
        legacyBscInformation.setBscSeries(outerNumber.getBscSeries());
        legacyBscInformation.setExternalNumber(outerNumber.getBscNumber());

        if (outerNumber instanceof OuterNumberWithBindingStatus) {
            OuterNumberWithBindingStatus outerNumberWithBindingStatus = (OuterNumberWithBindingStatus) outerNumber;
            legacyBscInformation.setBoundToPassenger(outerNumberWithBindingStatus.isBoundToPassenger());
        }

        return legacyBscInformation;

    }
}
