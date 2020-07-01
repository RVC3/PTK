package ru.ppr.cppk.ui.fragment.pd.simple.model;

import ru.ppr.cppk.ui.fragment.pd.simple.SimplePdFragment;

/**
 * Параметры для отображения {@link SimplePdFragment}.
 *
 * @author Aleksandr Brazhkin
 */
public class SimplePdViewParams {
    /**
     * Данные ПД
     */
    private TicketPdViewModel pdViewModel;
    /**
     * Флаг показа кнопки "ПД действует"
     */
    private boolean ticketValidBtnVisible;
    /**
     * Флаг показа кнопки "ПД НЕдействует"
     */
    private boolean ticketNotValidBtnVisible;
    /**
     * Флаг показа кнопки "Оформить доплату"
     */
    private boolean sellSurchargeBtnVisible;
    /**
     * Флаг показа кнопки "Оформить трансфер"
     */
    private boolean sellTransferBtnVisible;
    /**
     * Флаг использования верстки уменьшенного размера
     */
    private boolean smallSize;
    /**
     * Флаг возможности zoom-ирования
     */
    private boolean zoomEnabled;

    public TicketPdViewModel getPdViewModel() {
        return pdViewModel;
    }

    public void setPdViewModel(TicketPdViewModel pdViewModel) {
        this.pdViewModel = pdViewModel;
    }

    public boolean isTicketValidBtnVisible() {
        return ticketValidBtnVisible;
    }

    public void setTicketValidBtnVisible(boolean ticketValidBtnVisible) {
        this.ticketValidBtnVisible = ticketValidBtnVisible;
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

    public boolean isSellTransferBtnVisible() {
        return sellTransferBtnVisible;
    }

    public void setSellTransferBtnVisible(boolean sellTransferBtnVisible) {
        this.sellTransferBtnVisible = sellTransferBtnVisible;
    }

    public boolean isSmallSize() {
        return smallSize;
    }

    public void setSmallSize(boolean smallSize) {
        this.smallSize = smallSize;
    }

    public boolean isZoomEnabled() {
        return zoomEnabled;
    }

    public void setZoomEnabled(boolean zoomEnabled) {
        this.zoomEnabled = zoomEnabled;
    }
}
