package ru.ppr.core.dataCarrier.smartCard.parser.outerNumber;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumberWithBindingStatus;
import ru.ppr.utils.BcdUtils;

/**
 * Парсер внешнего номера карты для карт ЦППК на период, Тройка.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultOuterNumberParser extends BaseOuterNumberParser {

    private static final int OUTER_NUMBER_INDEX = 7;
    private static final int OUTER_NUMBER_LENGTH = 5;

    private static final int BOUND_TO_PASSENGER_INDEX = 15;

    @Override
    public OuterNumber parse(byte[] data) {
        OuterNumberWithBindingStatus outerNumber = new OuterNumberWithBindingStatus();
        fillBaseData(data, outerNumber);

        byte[] bscNumberData = DataCarrierUtils.subArray(data, OUTER_NUMBER_INDEX, OUTER_NUMBER_LENGTH);
        String bscNumber = BcdUtils.bcdToString(bscNumberData);

        byte boundToPassengerData = data[BOUND_TO_PASSENGER_INDEX];
        boolean boundToPassenger = DataCarrierUtils.byteToInt(boundToPassengerData) != 0;

        outerNumber.setBscNumber(bscNumber);
        outerNumber.setBoundToPassenger(boundToPassenger);

        return outerNumber;
    }
}
