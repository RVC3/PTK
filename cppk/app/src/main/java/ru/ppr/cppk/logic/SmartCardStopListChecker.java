package ru.ppr.cppk.logic;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.EnumSet;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.cppk.legacy.CardTypeToTicketStorageTypeMapper;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.repository.SmartCardCancellationReasonRepository;
import ru.ppr.security.entity.SmartCardStopListItem;
import ru.ppr.security.entity.StopCriteriaType;
import ru.ppr.security.repository.SmartCardStopListItemRepository;

/**
 * Класс, выполнющий поиск смарт-карты в стоп-листе.
 *
 * @author Aleksandr Brazhkin
 */
public class SmartCardStopListChecker {

    private static final String TAG = Logger.makeLogTag(SmartCardStopListChecker.class);

    private final SmartCardStopListItemRepository smartCardStopListItemRepository;
    private final SmartCardCancellationReasonRepository smartCardCancellationReasonRepository;

    @Inject
    public SmartCardStopListChecker(SmartCardStopListItemRepository smartCardStopListItemRepository,
                                    SmartCardCancellationReasonRepository smartCardCancellationReasonRepository) {
        this.smartCardStopListItemRepository = smartCardStopListItemRepository;
        this.smartCardCancellationReasonRepository = smartCardCancellationReasonRepository;
    }

    /**
     * Выполняет поиск смарт-карты в стоп-листе.
     *
     * @param cardInformation Информация о смарт-карте
     * @param nsiVersion      Версия НСИ
     * @return Запись стоп-листа
     */
    @Nullable
    public Pair<SmartCardStopListItem, String> findSmartCardStopListItem(CardInformation cardInformation, @Nullable EnumSet<StopCriteriaType> stopCriteriaTypes, int nsiVersion) {
        TicketStorageType ticketStorageType = new CardTypeToTicketStorageTypeMapper().map(cardInformation.getCardType());
        return findSmartCardStopListItem(ticketStorageType,
                cardInformation.getOuterNumberAsString(),
                cardInformation.getCrystalSerialNumberAsString(),
                stopCriteriaTypes,
                nsiVersion);
    }

    @Nullable
    public Pair<SmartCardStopListItem, String> findSmartCardStopListItem(TicketStorageType ticketStorageType,
                                                                         String outerNumberAsString,
                                                                         String crystalSerialNumberAsString,
                                                                         @Nullable EnumSet<StopCriteriaType> stopCriteriaTypes,
                                                                         int nsiVersion) {
        /*
        2016-01-22 код программного продукта
        switch (type)
        {
            case SmartCardType.CPPK:
            case SmartCardType.CPPKCounter:
            case SmartCardType.SKM:
            case SmartCardType.SKMO:
            case SmartCardType.SeeOfCard:
                query = query.Where(t => t.CrystalSerialNumber == uidNumber && t.OuterNumber == outerNumber);
                break;
            case SmartCardType.ETT:
                query = query.Where(t => t.OuterNumber == outerNumber);
                break;
            case SmartCardType.IPK:
            case SmartCardType.TRK:
                query = query.Where(t => t.CrystalSerialNumber == uidNumber);
                break;
        }
        */
        String crystalSerialNumber = null;
        String outerNumber = null;

        switch (ticketStorageType) {
            case ETT:
                outerNumber = outerNumberAsString;
                break;
            case IPK:
            case TRK:
            case STR:
            case SKMO: //https://aj.srvdev.ru/browse/CPPKPP-32551
                crystalSerialNumber = crystalSerialNumberAsString;
                break;
            case CPPK:
            case CPPKCounter:
            case SKM:
            case SeeOfCard:
            default:
                outerNumber = outerNumberAsString;
                crystalSerialNumber = crystalSerialNumberAsString;
                break;
        }

        SmartCardStopListItem smartCardStopListItem = smartCardStopListItemRepository
                .findStopListItemForSmartCard(
                        ticketStorageType.getDBCode(),
                        crystalSerialNumber,
                        outerNumber,
                        stopCriteriaTypes
                );

        Logger.trace(TAG, "ticketStorageType: " + ticketStorageType.getDBCode() + " " + ticketStorageType.getAbbreviation());
        Logger.trace(TAG, "crystalSerialNumber: " + crystalSerialNumber);
        Logger.trace(TAG, "outerNumber: " + outerNumber);
        Logger.trace(TAG, "nsiVersion: " + nsiVersion);

        if (smartCardStopListItem == null) {
            return null;
        }

        String reason = null;

        if (smartCardStopListItem.getReasonCode() >= 0) {
            reason = smartCardCancellationReasonRepository.getReasonForCode(smartCardStopListItem.getReasonCode(), nsiVersion);
        }

        if (reason == null) {
            reason = "Причина неизвестна";
        }

        Logger.trace(TAG, "reason: " + reason);

        return new Pair<>(smartCardStopListItem, reason);

    }
}
