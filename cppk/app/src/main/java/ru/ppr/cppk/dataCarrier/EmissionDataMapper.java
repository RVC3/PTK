package ru.ppr.cppk.dataCarrier;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import ru.ppr.core.dataCarrier.smartCard.entity.EmissionData;

/**
 * @author Aleksandr Brazhkin
 */
public class EmissionDataMapper {

    public EmissionDataMapper() {

    }

    public ru.ppr.cppk.dataCarrier.entity.EmissionData toLegacyEmissionData(EmissionData emissionData) {
        ru.ppr.cppk.dataCarrier.entity.EmissionData legacyEmissionData = new ru.ppr.cppk.dataCarrier.entity.EmissionData();

        legacyEmissionData.setFormatVersion(emissionData.getVersion());
        legacyEmissionData.setCardNumber(emissionData.getCardNumber());
        legacyEmissionData.setCardSeries(emissionData.getCardSeries());
        legacyEmissionData.setValidityTime(parseValidityTime(emissionData.getCardSeries()));
        legacyEmissionData.setOrderNumber(Integer.valueOf(emissionData.getCardSeries().substring(4, 6)));
        legacyEmissionData.setRegionCode(Integer.valueOf(emissionData.getCardSeries().substring(6, 8)));
        legacyEmissionData.setControlSum(emissionData.getControlSum());

        return legacyEmissionData;
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
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        return calendar.getTime();
    }
}
