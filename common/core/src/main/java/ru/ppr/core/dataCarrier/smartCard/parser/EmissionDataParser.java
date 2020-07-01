package ru.ppr.core.dataCarrier.smartCard.parser;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.entity.EmissionData;
import ru.ppr.utils.BcdUtils;

/**
 * Парсер эмисионных данных для карт СКМ, СКМО, ИПК.
 *
 * @author Aleksandr Brazhkin
 */
public class EmissionDataParser {

    private static final int VERSION_INDEX = 0;
    private static final int CARD_NUMBER_INDEX = 1;
    private static final int CARD_NUMBER_LENGTH = 10;
    private static final int CARD_SERIES_INDEX = 11;
    private static final int CARD_SERIES_LENGTH = 4;
    private static final int CONTROL_SUM_INDEX = 15;

    public EmissionDataParser() {

    }

    /**
     * Парсит эмисионные данные.
     *
     * @param data Данные ПД
     * @return Эмисионные данные
     */
    @NonNull
    public EmissionData parse(@NonNull byte[] data) {

        EmissionData emissionData = new EmissionData();

        if (data.length < 16)
            return emissionData;

        byte versionData = data[VERSION_INDEX];
        int version = BcdUtils.bcdToInt(versionData);

        byte[] cardNumberData = DataCarrierUtils.subArray(data, CARD_NUMBER_INDEX, CARD_NUMBER_LENGTH);
        // Последняя цифра - код Luhn, контрольная сумма от всех предыдущих цифр, может быть > 9
        // Очистим последнюю цифру, иначе возможно искажение предпоследней, если контрольная сумма > 9
        cardNumberData[CARD_NUMBER_LENGTH - 1] = (byte) (cardNumberData[CARD_NUMBER_LENGTH - 1] & 0xf0);
        String cardNumber = BcdUtils.bcdToString(cardNumberData).substring(0, 19);

        byte[] cardSeriesData = DataCarrierUtils.subArray(data, CARD_SERIES_INDEX, CARD_SERIES_LENGTH);
        String cardSeries = BcdUtils.bcdToString(cardSeriesData);

        byte controlSumData = data[CONTROL_SUM_INDEX];
        int controlSum = (int) controlSumData;

        emissionData.setVersion(version);
        emissionData.setCardNumber(cardNumber);
        emissionData.setCardSeries(cardSeries);
        emissionData.setControlSum(controlSum);

        return emissionData;
    }
}
