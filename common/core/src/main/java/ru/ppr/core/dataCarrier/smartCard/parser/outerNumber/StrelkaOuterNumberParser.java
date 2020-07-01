package ru.ppr.core.dataCarrier.smartCard.parser.outerNumber;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumberWithBindingStatus;
import ru.ppr.utils.BcdUtils;

/**
 * Парсер внешнего номера карты для карт Стрелка.
 *
 * @author Aleksandr Brazhkin
 */
public class StrelkaOuterNumberParser extends BaseOuterNumberParser {

    private static final int OUTER_NUMBER_INDEX = 6;
    private static final int OUTER_NUMBER_LENGTH = 6;

    private static final int BOUND_TO_PASSENGER_INDEX = 15;

    @Override
    public OuterNumber parse(byte[] data) {
        OuterNumberWithBindingStatus outerNumber = new OuterNumberWithBindingStatus();
        fillBaseData(data, outerNumber);

        byte[] bscNumberData = DataCarrierUtils.subArray(data, OUTER_NUMBER_INDEX, OUTER_NUMBER_LENGTH);
        String bscNumber = BcdUtils.bcdToString(bscNumberData);
        //https://aj.srvdev.ru/browse/CPPKPP-30178
        bscNumber = bscNumber.substring(1);

        byte boundToPassengerData = data[BOUND_TO_PASSENGER_INDEX];
        boolean boundToPassengerStatus = DataCarrierUtils.byteToInt(boundToPassengerData) != 0;

        outerNumber.setBscNumber(bscNumber);
        outerNumber.setBoundToPassenger(boundToPassengerStatus);

        return outerNumber;
    }
}
