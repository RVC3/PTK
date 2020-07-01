package ru.ppr.cppk.ui.activity.controlreadbsc;


import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface ControlReadBscView extends MvpView {

    void setTimerValue(int value);

    void setState(State state);

    void showSaleNewPdConfirmDialog();

    void setSaleNewPdBtnVisible(boolean visible);

    enum State {
        SEARCH_CARD,
        READ_CARD,
        PROCESSING_DATA,
        SEARCH_CARD_ERROR,
        UNKNOWN_ERROR
    }
}
