package ru.ppr.chit.ui.activity.readbarcode;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Grigoriy Kashka
 */
interface ReadBarcodeView extends MvpView {

    void setTimerValue(int value);

    void setState(State state);

    enum State {
        SEARCH_BARCODE,
        PROCESSING_DATA,
        SEARCH_BARCODE_ERROR,
        UNKNOWN_ERROR
    }
}
