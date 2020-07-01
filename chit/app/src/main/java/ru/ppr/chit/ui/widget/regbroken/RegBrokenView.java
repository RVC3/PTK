package ru.ppr.chit.ui.widget.regbroken;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Dmitry Nevolin
 */
interface RegBrokenView extends MvpView {

    void setIndicatorVisible(boolean visible);
    void showConnectionBrokenError();

}
