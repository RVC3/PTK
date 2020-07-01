package ru.ppr.chit.ui.activity.readbsqrcode;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Dmitry Nevolin
 */
interface ReadBsQrCodeView extends MvpView {

    void setTimerValue(int value);

    void setState(State state);

    enum State {
        SEARCH_BARCODE,
        PROCESSING_DATA,
        SEARCH_BARCODE_ERROR,
        UNKNOWN_ERROR
    }

}
