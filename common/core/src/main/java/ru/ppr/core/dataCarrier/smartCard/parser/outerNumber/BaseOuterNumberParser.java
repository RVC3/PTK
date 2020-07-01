package ru.ppr.core.dataCarrier.smartCard.parser.outerNumber;

import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.entity.BscType;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.utils.BcdUtils;

/**
 * Парсер внешнего номера карты для карт ЦППК на период, Тройка, Стрелка.
 *
 * @author Aleksandr Brazhkin
 */
abstract class BaseOuterNumberParser implements OuterNumberParser {

    private static final int TYPE_BSC_INDEX = 4;
    private static final int VALIDITY_TERM_INDEX = 0;
    private static final int VALIDITY_TERM_LENGTH = 2;
    private static final int INIT_DATE_INDEX = 2;
    private static final int INIT_DATE_LENGTH = 2;
    private static final int BSC_SERIES_INDEX = 5;
    private static final int BSC_SERIES_LENGTH = 2;

    @Override
    public abstract OuterNumber parse(byte[] data);

    final void fillBaseData(byte[] data, OuterNumber outerNumber) {
        if (data.length == 16) {

            byte bscTypeData = data[TYPE_BSC_INDEX];
            BscType bscType = BscType.getByRawCode(bscTypeData);

            byte[] validityTermData = DataCarrierUtils.subArray(data, VALIDITY_TERM_INDEX, VALIDITY_TERM_LENGTH);
            int validityTermInt = DataCarrierUtils.bytesToInt(validityTermData, ByteOrder.LITTLE_ENDIAN);
            Date validityTerm = parseDate(validityTermInt);

            byte[] initDateData = DataCarrierUtils.subArray(data, INIT_DATE_INDEX, INIT_DATE_LENGTH);
            int initDateInt = DataCarrierUtils.bytesToInt(initDateData, ByteOrder.LITTLE_ENDIAN);
            Date initDate = parseDate(initDateInt);

            byte[] bscSeriesData = DataCarrierUtils.subArray(data, BSC_SERIES_INDEX, BSC_SERIES_LENGTH);
            String bscSeries = BcdUtils.bcdToString(bscSeriesData);

            outerNumber.setBscType(bscType);
            outerNumber.setValidityTerm(validityTerm);
            outerNumber.setInitDate(initDate);
            outerNumber.setBscSeries(bscSeries);
        }
    }

    /**
     * Парсит дату из числа {@code intDate}
     * Целое число, упаковка "Little Endian". Пример: байт 2 страницы 4 = 0x41, байт 3 страницы 4 = 0x1E, записана дата 01.02.2015.
     * Биты 0-4: день месяца (1 = 1-ое число).
     * Биты 5-8: месяц (1 = январь).
     * Биты 9-15: год, начиная с 2000 (0 = 2000, 1 = 2001).
     *
     * @param intDate
     * @return
     */
    private Date parseDate(int intDate) {
        int day = intDate & 0x1F;
        int month = (intDate >> 5) & 0x0F;
        int year = (intDate >> 9) & 0x7F;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year + 2000, month - 1, day);

        Date date = calendar.getTime();
        return date;
    }
}
