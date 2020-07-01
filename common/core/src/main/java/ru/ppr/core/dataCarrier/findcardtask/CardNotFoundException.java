package ru.ppr.core.dataCarrier.findcardtask;

/**
 * Исключение возникающие при ошибке поиска карты.
 *
 * @author Aleksandr Brazhkin
 */
public class CardNotFoundException extends Exception {
    public CardNotFoundException() {
    }

    public CardNotFoundException(String message) {
        super(message);
    }

    public CardNotFoundException(Throwable cause) {
        super(cause);
    }
}
