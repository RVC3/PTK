package ru.ppr.cppk.legacy;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardType;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * Маппер типа карты со слоя data carrier в {@link TicketStorageType}.
 *
 * @author Aleksandr Brazhkin
 */
public class CardTypeToTicketStorageTypeMapper {

    @NonNull
    public TicketStorageType map(@Nullable CardType cardType) {
        if (cardType == null) {
            return TicketStorageType.Unknown;
        }
        switch (cardType) {
            case SKM:
                return TicketStorageType.SKM;
            case SKMO:
                return TicketStorageType.SKMO;
            case IPK:
                return TicketStorageType.IPK;
            case ETT:
                return TicketStorageType.ETT;
            case TRK:
                return TicketStorageType.TRK;
            case CPPK:
                return TicketStorageType.CPPK;
            case CPPK_COUNTER:
                return TicketStorageType.CPPKCounter;
            case SEE_OF_CARD:
                return TicketStorageType.SeeOfCard;
            case SERVICE:
                return TicketStorageType.Service;
            case STRELKA:
                return TicketStorageType.STR;
            case SCM_SCMO_or_IPK_BSC:
            default:
                return TicketStorageType.Unknown;
        }
    }
}
