package ru.ppr.chit.ui.activity.readbsc;


import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
public interface ReadBscView extends MvpView {

    void setTimerValue(int value);

    void setState(State state);

    enum State {
        SEARCH_CARD,
        READ_CARD,
        PROCESSING_DATA,
        EMPTY_CARD,
        SEARCH_CARD_ERROR,
        UNKNOWN_ERROR
    }
}
