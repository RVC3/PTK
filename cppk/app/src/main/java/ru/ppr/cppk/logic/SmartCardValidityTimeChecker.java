package ru.ppr.cppk.logic;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.legacy.CardTypeToTicketStorageTypeMapper;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * @author Aleksandr Brazhkin
 */
public class SmartCardValidityTimeChecker {

    private static final String TAG = Logger.makeLogTag(SmartCardValidityTimeChecker.class);

    private final CommonSettings commonSettings;

    @Inject
    public SmartCardValidityTimeChecker(CommonSettings commonSettings) {
        this.commonSettings = commonSettings;
    }

    /**
     * Производит проверку даты действия карты и возвращает результат.
     * Билеты, записанные на ультралайт(провожающего, абонемент на количество поездок)
     * всегда действительны.
     *
     * @param cardInformation Информация о смарт-карте
     * @param forDate         Дата, на которую выполняется проверка валдиности смарт-карты
     * @return {@code true} - карта действует {@code false} - время действия карты истекло.
     */
    public boolean isCardTimeValid(CardInformation cardInformation, Date forDate, boolean serviceTicket) {
        TicketStorageType ticketStorageType = new CardTypeToTicketStorageTypeMapper().map(cardInformation.getCardType());
        Logger.trace(TAG, "ticketStorageType: " + ticketStorageType.getDBCode() + " " + ticketStorageType.getAbbreviation());
        Logger.trace(TAG, "cardInformation.getExpiryDate(): " + DateFormatOperations.getDateddMMyyyyHHmmss(cardInformation.getExpiryDate()));
        Logger.trace(TAG, "forDate: " + DateFormatOperations.getDateddMMyyyyHHmmss(forDate));
        Logger.trace(TAG, "commonSettings.isIgnoreCardValidityPeriod(): " + commonSettings.isIgnoreCardValidityPeriod());
        return ticketStorageType == TicketStorageType.SeeOfCard
                || ticketStorageType == TicketStorageType.CPPKCounter
                || serviceTicket
                || cardInformation.getExpiryDate().after(forDate)
                //https://aj.srvdev.ru/browse/CPPKPP-31895
                || commonSettings.isIgnoreCardValidityPeriod();
    }
}
