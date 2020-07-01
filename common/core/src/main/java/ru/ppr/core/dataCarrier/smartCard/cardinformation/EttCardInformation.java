package ru.ppr.core.dataCarrier.smartCard.cardinformation;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;
import ru.ppr.core.dataCarrier.smartCard.entity.EttData;

/**
 * @author Aleksandr Brazhkin
 */
public class EttCardInformation extends CardInformation {

    private final BscInformation bscInformation;
    private final EttData ettData;

    public EttCardInformation(CardInfo cardInfo, BscInformation bscInformation, EttData ettData) {
        super(cardInfo);
        this.bscInformation = bscInformation;
        this.ettData = ettData;
    }

    public BscInformation getBscInformation() {
        return bscInformation;
    }

    public EttData getEttData() {
        return ettData;
    }

    @Override
    public CardType getCardType() {
        return CardType.valueOf(getBscInformation().getBscType());
    }

    @Override
    public String getOuterNumberAsString() {
        // Для ЭТТ удаляем контрольную цифру
        return getBscInformation().getBscNumber().substring(0, 13);
    }

    @Override
    public String getOuterNumberAsFormattedString() {
        return getBscInformation().getBscNumber();
    }

    @Override
    public Date getExpiryDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int year = getBscInformation().getEndYear();
        // вычитаем 1 т.к. в Calendar индекс января ==0
        int month = getBscInformation().getEndMonth();
        calendar.set(2000 + year, month, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.SECOND, -1);
        return calendar.getTime();
    }
}
