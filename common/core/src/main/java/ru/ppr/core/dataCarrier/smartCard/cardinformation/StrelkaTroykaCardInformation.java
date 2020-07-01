package ru.ppr.core.dataCarrier.smartCard.cardinformation;

import java.util.Date;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;

/**
 * @author Aleksandr Brazhkin
 */
public class StrelkaTroykaCardInformation extends CardInformation {

    private final OuterNumber outerNumber;

    public StrelkaTroykaCardInformation(CardInfo cardInfo, OuterNumber outerNumber) {
        super(cardInfo);
        this.outerNumber = outerNumber;
    }

    public OuterNumber getOuterNumber() {
        return outerNumber;
    }

    @Override
    public CardType getCardType() {
        return CardType.valueOf(getOuterNumber().getBscType());
    }

    @Override
    public String getOuterNumberAsString() {
        // Для Тройки и Стрелки номер должен состоять из 10 цифр
        return getOuterNumber().getBscNumber();
    }

    @Override
    public String getOuterNumberAsFormattedString() {
        return getOuterNumber().getBscNumber();
    }

    @Override
    public Date getExpiryDate() {
        return getOuterNumber().getValidityTerm();
    }
}
