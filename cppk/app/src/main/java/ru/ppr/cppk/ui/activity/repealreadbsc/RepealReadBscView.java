package ru.ppr.cppk.ui.activity.repealreadbsc;


import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface RepealReadBscView extends MvpView {

    void setTimerValue(int value);

    void setState(State state);

    enum State {
        SEARCH_CARD,
        READ_CARD,
        PROCESSING_DATA,
        SEARCH_CARD_ERROR,
        UNKNOWN_ERROR
    }
}
