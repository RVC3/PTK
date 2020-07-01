package ru.ppr.core.dataCarrier.smartCard.parser;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;
import ru.ppr.core.dataCarrier.smartCard.entity.BscType;
import ru.ppr.utils.BcdUtils;

/**
 * Парсер информации о БСК для карт СКМ, СКМО, ИПК, ЭТТ.
 *
 * @author Aleksandr Brazhkin
 */
public class BscInformationParser {

    private static final int TYPE_BSC_INDEX = 4;
    private static final int END_MONTH_INDEX = 2;
    private static final int END_YEAR_INDEX = 3;
    private static final int EXEMPTION_INDEX = 0;
    private static final int EXEMPTION_LENGTH = 2;
    private static final int BSC_NUMBER_INDEX = 5;
    private static final int BSC_NUMBER_LENGTH = 7;

    public BscInformationParser() {

    }

    /**
     * Парсит информацию о БСК.
     *
     * @param data Данные ПД
     * @return Информация о БСК
     */
    @NonNull
    public BscInformation parse(@NonNull byte[] data) {

        BscInformation bscInformation = new BscInformation();

        if (data.length < 16)
            return bscInformation;

        byte bscTypeData = data[TYPE_BSC_INDEX];
        BscType bscType = BscType.getByRawCode(bscTypeData);

        byte endMonthData = data[END_MONTH_INDEX];
        int endMonth = BcdUtils.bcdToInt(endMonthData);

        byte endYearData = data[END_YEAR_INDEX];
        int endYear = BcdUtils.bcdToInt(endYearData);

        byte[] exemptionCodeData = DataCarrierUtils.subArray(data, EXEMPTION_INDEX, EXEMPTION_LENGTH);
        int exemptionCode = BcdUtils.bcdToInt(exemptionCodeData);

        byte[] bscNumberData = DataCarrierUtils.subArray(data, BSC_NUMBER_INDEX, BSC_NUMBER_LENGTH);
        String bscNumber = BcdUtils.bcdToString(bscNumberData);

        bscInformation.setBscType(bscType);
        bscInformation.setEndMonth(endMonth);
        bscInformation.setEndYear(endYear);
        bscInformation.setExemptionCode(exemptionCode);
        bscInformation.setBscNumber(bscNumber);

        return bscInformation;
    }
}
