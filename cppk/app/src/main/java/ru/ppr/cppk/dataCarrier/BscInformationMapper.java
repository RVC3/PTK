package ru.ppr.cppk.dataCarrier;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardType;
import ru.ppr.cppk.legacy.CardTypeToTicketStorageTypeMapper;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * @author Aleksandr Brazhkin
 */
public class BscInformationMapper {

    public BscInformationMapper() {

    }

    public ru.ppr.cppk.dataCarrier.entity.BscInformation toLegacyBscInformation(BscInformation bscInformation, byte[] cardUid) {

        ru.ppr.cppk.dataCarrier.entity.BscInformation legacyBscInformation = new ru.ppr.cppk.dataCarrier.entity.BscInformation();
        legacyBscInformation.setCardUID(cardUid);

        CardType cardType = CardType.valueOf(bscInformation.getBscType());
        TicketStorageType ticketStorageType = new CardTypeToTicketStorageTypeMapper().map(cardType);
        legacyBscInformation.setTypeBsc(ticketStorageType);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int year = bscInformation.getEndYear();
        // вычитаем 1 т.к. в Calendar индекс января ==0
        int month = bscInformation.getEndMonth();
        calendar.set(2000 + year, month, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.SECOND, -1);

        legacyBscInformation.setValidityTime(calendar.getTime());
        legacyBscInformation.setInitDate(new Date(0));
        legacyBscInformation.setExemptionCode(bscInformation.getExemptionCode());
        legacyBscInformation.setBscSeries("");
        legacyBscInformation.setExternalNumber(bscInformation.getBscNumber());

        return legacyBscInformation;
    }
}
