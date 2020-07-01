package ru.ppr.core.dataCarrier.smartCard.cardinformation;

import java.util.Date;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;

/**
 * @author Aleksandr Brazhkin
 */
public class CppkCardInformation extends CardInformation {

    private final OuterNumber outerNumber;

    public CppkCardInformation(CardInfo cardInfo, OuterNumber outerNumber) {
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
        return getOuterNumber().getBscSeries() + getOuterNumber().getBscNumber();
    }

    @Override
    public String getOuterNumberAsFormattedString() {
        // Убрал исскуственное форматирование в соответствии с требованиями по задаче CPPKPP-40612
        return getOuterNumber().getBscSeries() + getOuterNumber().getBscNumber();
    }

    @Override
    public Date getExpiryDate() {
        return getOuterNumber().getValidityTerm();
    }
}
