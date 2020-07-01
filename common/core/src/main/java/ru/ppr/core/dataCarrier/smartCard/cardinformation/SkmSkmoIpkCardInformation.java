package ru.ppr.core.dataCarrier.smartCard.cardinformation;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.checker.SkmSkmoIpkRecognizer;
import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;
import ru.ppr.core.dataCarrier.smartCard.entity.EmissionData;
import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;

/**
 * @author Aleksandr Brazhkin
 */
public class SkmSkmoIpkCardInformation extends CardInformation {

    private final BscInformation bscInformation;
    private final EmissionData emissionData;
    private final PersonalData personalData;

    public SkmSkmoIpkCardInformation(CardInfo cardInfo, BscInformation bscInformation, EmissionData emissionData, PersonalData personalData) {
        super(cardInfo);
        this.bscInformation = bscInformation;
        this.emissionData = emissionData;
        this.personalData = personalData;
    }

    public BscInformation getBscInformation() {
        return bscInformation;
    }

    public EmissionData getEmissionData() {
        return emissionData;
    }

    public PersonalData getPersonalData() {
        return personalData;
    }

    @Override
    public CardType getCardType() {
        SkmSkmoIpkRecognizer skmSkmoIpkRecognizer = new SkmSkmoIpkRecognizer();
        if (getEmissionData() != null) {
            if (skmSkmoIpkRecognizer.isIpk(getEmissionData().getCardNumber()))
                return CardType.IPK;
            if (skmSkmoIpkRecognizer.isSkm(getEmissionData().getCardNumber()))
                return CardType.SKM;
            if (skmSkmoIpkRecognizer.isSkmo(getEmissionData().getCardNumber()))
                return CardType.SKMO;
        }
        return CardType.valueOf(getBscInformation().getBscType());
    }

    @Override
    public String getOuterNumberAsString() {
        return getEmissionData().getCardNumber();
    }

    @Override
    public String getOuterNumberAsFormattedString() {
        return getEmissionData().getCardNumber();
    }

    @Override
    public Date getExpiryDate() {
        return parseValidityTime(getEmissionData().getCardSeries());
    }

    private Date parseValidityTime(String cardSeries) {

        int rawYear = Integer.valueOf(cardSeries.substring(0, 2));
        int ramMonth = Integer.valueOf(cardSeries.substring(2, 4));

        int year;
        int month;

        // На некоторых старых картах формат записи ММГГ, а на новых формат ГГММ,
        // поэтому проверим если значения месяца больше 12,
        // то значит дата записанна в старом формате(ММГГ)
        if (ramMonth > 12) {
            year = 2000 + ramMonth;
            month = rawYear - 1;
        } else {
            year = 2000 + rawYear;
            month = ramMonth - 1;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        calendar.set(year, month, 1, 23, 59, 59);
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        return calendar.getTime();
    }
}
