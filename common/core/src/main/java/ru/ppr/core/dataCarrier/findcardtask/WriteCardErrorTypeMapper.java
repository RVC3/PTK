package ru.ppr.core.dataCarrier.findcardtask;

import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardErrorType;
import ru.ppr.rfid.WriteToCardResult;

/**
 * Конвертер {@link WriteToCardResult} в {@link WriteCardErrorType}
 *
 * @author Aleksandr Brazhkin
 */
public class WriteCardErrorTypeMapper {

    /**
     * Конвертирует {@link WriteToCardResult} в {@link WriteCardErrorType}
     *
     * @param writeToCardResult Тип ошибки записи на карту с нижнего уровня
     * @return Тип ошибки записи на карту
     */
    public static WriteCardErrorType map(WriteToCardResult writeToCardResult) {
        switch (writeToCardResult) {
            case SUCCESS:
                return WriteCardErrorType.SUCCESS;
            case UID_DOES_NOT_MATCH:
                return WriteCardErrorType.UID_DOES_NOT_MATCH;
            case CAN_NOT_SEARCH_CARD:
                return WriteCardErrorType.CAN_NOT_SEARCH_CARD;
            case UNKNOWN_ERROR:
                return WriteCardErrorType.UNKNOWN_ERROR;
            case WRITE_ERROR:
                return WriteCardErrorType.WRITE_ERROR;
            case NOT_SUPPORTED:
                return WriteCardErrorType.NOT_SUPPORTED;
            default:
                return WriteCardErrorType.UNKNOWN_ERROR;
        }
    }
}
