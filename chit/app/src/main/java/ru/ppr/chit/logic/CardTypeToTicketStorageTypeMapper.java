package ru.ppr.chit.logic;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.nsi.TicketStorageType;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardType;

/**
 * Маппер типа карты со слоя data carrier в {@link TicketStorageType}.
 *
 * @author Aleksandr Brazhkin
 */
public class CardTypeToTicketStorageTypeMapper {

    @Inject
    CardTypeToTicketStorageTypeMapper(){

    }

    @NonNull
    public TicketStorageType map(@Nullable CardType cardType) {
        if (cardType == null) {
            return TicketStorageType.UNKNOWN;
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
                return TicketStorageType.CPPK_COUNTER;
            case SEE_OF_CARD:
                return TicketStorageType.SEE_OF_CARD;
            case SERVICE:
                return TicketStorageType.SERVICE;
            case STRELKA:
                return TicketStorageType.STRELKA;
            case SCM_SCMO_or_IPK_BSC:
            default:
                return TicketStorageType.UNKNOWN;
        }
    }
}
