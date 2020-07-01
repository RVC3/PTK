package ru.ppr.cppk.ui.fragment.pd.simple.model;

/**
 * Базовый класс для модели ПД, отображаемой во View.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BasePdViewModel {
    /**
     * Номер ПД
     */
    private int number;
    /**
     * Наименование ПД
     */
    private String title;
    /**
     * Флаг валидности ПД
     */
    private boolean valid;
    /**
     * Флаг наличия ошибки
     * "Ошибка цифровой подписи"
     */
    private boolean invalidEdsKeyError;
    /**
     * Флаг наличия ошибки
     * "Ключ ЭЦП отозван"
     */
    private boolean revokedEdsKeyError;
    /**
     * Флаг наличия ошибки
     * "Билет в стоп-листе"
     */
    private boolean ticketInStopListError;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isInvalidEdsKeyError() {
        return invalidEdsKeyError;
    }

    public void setInvalidEdsKeyError(boolean invalidEdsKeyError) {
        this.invalidEdsKeyError = invalidEdsKeyError;
    }

    public boolean isRevokedEdsKeyError() {
        return revokedEdsKeyError;
    }

    public void setRevokedEdsKeyError(boolean revokedEdsKeyError) {
        this.revokedEdsKeyError = revokedEdsKeyError;
    }

    public boolean isTicketInStopListError() {
        return ticketInStopListError;
    }

    public void setTicketInStopListError(boolean ticketInStopListError) {
        this.ticketInStopListError = ticketInStopListError;
    }
}
