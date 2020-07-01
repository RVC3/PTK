package ru.ppr.core.dataCarrier.findcardtask;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardErrorType;
import ru.ppr.rfid.CardReadErrorType;

/**
 * Конвертер {@link CardReadErrorType} в {@link ReadCardErrorType}
 *
 * @author Aleksandr Brazhkin
 */
public class ReadCardErrorTypeMapper {

    /**
     * Конвертирует {@link CardReadErrorType} в {@link ReadCardErrorType}
     *
     * @param cardReadErrorType Тип ошибки чтения карты с нижнего уровня
     * @return Тип ошибки чтения карты
     */
    public static ReadCardErrorType map(CardReadErrorType cardReadErrorType) {
        switch (cardReadErrorType) {
            case NONE:
                return ReadCardErrorType.NONE;
            case AUTHORIZATION:
                return ReadCardErrorType.AUTHORIZATION;
            case OTHER:
                return ReadCardErrorType.OTHER;
            case UID_DOES_NOT_MATCH:
                return ReadCardErrorType.UID_DOES_NOT_MATCH;
            default:
                return ReadCardErrorType.OTHER;
        }
    }
}
