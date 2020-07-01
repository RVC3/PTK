package ru.ppr.core.dataCarrier.smartCard.parser.outerNumber;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.utils.BcdUtils;

/**
 * Парсер внешнего номера карты для карт ЦППК на количество.
 *
 * @author Aleksandr Brazhkin
 */
public class CppkNumberOfTripsOuterNumberParser extends BaseOuterNumberParser {

    private static final int OUTER_NUMBER_INDEX = 7;
    private static final int OUTER_NUMBER_LENGTH = 5;

    @Override
    public OuterNumber parse(byte[] data) {
        OuterNumber outerNumber = new OuterNumber();
        fillBaseData(data, outerNumber);

        byte[] bscNumberData = DataCarrierUtils.subArray(data, OUTER_NUMBER_INDEX, OUTER_NUMBER_LENGTH);
        String bscNumber = BcdUtils.bcdToString(bscNumberData);

        outerNumber.setBscNumber(bscNumber);

        return outerNumber;
    }
}
