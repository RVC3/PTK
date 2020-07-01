package ru.ppr.core.dataCarrier.smartCard.checker;

/**
 * Вспомогательный класс, различающий карты СКМ, СКМО, ИПК.
 *
 * @author Aleksandr Brazhkin
 */
public class SkmSkmoIpkRecognizer {

    private static final String SKM_CODE = "96439077";
    private static final String SKMO_CODE = "96439090";
    private static final String IPK_CODE = "964391";

    public SkmSkmoIpkRecognizer() {

    }

    /**
     * Проверяет, является ли карта картой СКМ.
     *
     * @param cardNumber Номер карты из эмисионных данных
     * @return {@code true}, если это карта СКМ, {@code false} иначе.
     */
    public boolean isSkm(String cardNumber) {
        return cardNumber != null && cardNumber.startsWith(SKM_CODE);
    }

    /**
     * Проверяет, является ли карта картой СКМО.
     *
     * @param cardNumber Номер карты из эмисионных данных
     * @return {@code true}, если это карта СКМО, {@code false} иначе.
     */
    public boolean isSkmo(String cardNumber) {
        return cardNumber != null && cardNumber.startsWith(SKMO_CODE);
    }

    /**
     * Проверяет, является ли карта картой ИПК.
     *
     * @param cardNumber Номер карты из эмисионных данных
     * @return {@code true}, если это карта ИПК, {@code false} иначе.
     */
    public boolean isIpk(String cardNumber) {
        return cardNumber != null && cardNumber.startsWith(IPK_CODE);
    }
}
