package ru.ppr.cppk.ui.fragment.pd.countrips.model;

import ru.ppr.cppk.ui.fragment.pd.countrips.CountTripsFragment;
import ru.ppr.cppk.ui.fragment.pd.simple.model.CountTripsPdViewModel;

/**
 * Параметры для отображения {@link CountTripsFragment}.
 *
 * @author Aleksandr Brazhkin
 */
public class CountTripsPdViewParams {
    /**
     * Данные ПД
     */
    private CountTripsPdViewModel pdViewModel;
    /**
     * Флаг показа кнопки "Списать поездку"
     */
    private boolean decrementTripBtnVisible;
    /**
     * Флаг показа кнопки "ПД НЕдействует"
     */
    private boolean ticketNotValidBtnVisible;
    /**
     * Флаг показа кнопки "Оформить доплату"
     */
    private boolean sellSurchargeBtnVisible;
    /**
     * Ошибка "Некорректные данные"
     */
    private boolean invalidData;
    /**
     * Флаг использования верстки уменьшенного размера
     */
    private boolean smallSize;
    /**
     * Флаг использования кнопки "Списать поездку на 7000-ый поезд"
     */
    private boolean decrement7000;
    /**
     * Флаг использования кнопки "Исправить метку прохода"
     */
    private boolean fixPassageMark;

    public CountTripsPdViewModel getPdViewModel() {
        return pdViewModel;
    }

    public void setPdViewModel(CountTripsPdViewModel pdViewModel) {
        this.pdViewModel = pdViewModel;
    }

    public boolean isDecrementTripBtnVisible() {
        return decrementTripBtnVisible;
    }

    public void setDecrementTripBtnVisible(boolean decrementTripBtnVisible) {
        this.decrementTripBtnVisible = decrementTripBtnVisible;
    }

    public boolean isTicketNotValidBtnVisible() {
        return ticketNotValidBtnVisible;
    }

    public void setTicketNotValidBtnVisible(boolean ticketNotValidBtnVisible) {
        this.ticketNotValidBtnVisible = ticketNotValidBtnVisible;
    }

    public boolean isSellSurchargeBtnVisible() {
        return sellSurchargeBtnVisible;
    }

    public void setSellSurchargeBtnVisible(boolean sellSurchargeBtnVisible) {
        this.sellSurchargeBtnVisible = sellSurchargeBtnVisible;
    }

    public boolean isInvalidData() {
        return invalidData;
    }

    public void setInvalidData(boolean invalidData) {
        this.invalidData = invalidData;
    }

    public boolean isSmallSize() {
        return smallSize;
    }

    public void setSmallSize(boolean smallSize) {
        this.smallSize = smallSize;
    }

    public boolean isDecrement7000() {
        return decrement7000;
    }

    public void setDecrement7000(boolean decrement7000) {
        this.decrement7000 = decrement7000;
    }

    public boolean isFixPassageMark() {
        return fixPassageMark;
    }

    public void setFixPassageMark(boolean fixPassageMark) {
        this.fixPassageMark = fixPassageMark;
    }
}
