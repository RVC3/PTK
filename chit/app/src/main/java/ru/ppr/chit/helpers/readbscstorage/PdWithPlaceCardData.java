package ru.ppr.chit.helpers.readbscstorage;

import ru.ppr.core.dataCarrier.pd.base.PdWithPlace;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;

/**
 * Данные, считанные с карты, если на карте был ПД с местом.
 *
 * @author Aleksandr Brazhkin
 */
public class PdWithPlaceCardData {
    /**
     * ПД с местом
     */
    private PdWithPlace pdWithPlace;
    /**
     * Подпись
     */
    private byte[] eds;
    /**
     * Информация о карте
     */
    private CardInformation cardInformation;

    public PdWithPlace getPdWithPlace() {
        return pdWithPlace;
    }

    public void setPdWithPlace(PdWithPlace pdWithPlace) {
        this.pdWithPlace = pdWithPlace;
    }

    public byte[] getEds() {
        return eds;
    }

    public void setEds(byte[] eds) {
        this.eds = eds;
    }

    public CardInformation getCardInformation() {
        return cardInformation;
    }

    public void setCardInformation(CardInformation cardInformation) {
        this.cardInformation = cardInformation;
    }
}
