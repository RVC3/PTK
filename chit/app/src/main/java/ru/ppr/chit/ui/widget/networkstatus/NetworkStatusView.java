package ru.ppr.chit.ui.widget.networkstatus;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Dmitry Nevolin
 */
interface NetworkStatusView extends MvpView {

    void setNetworkAvailable(boolean value);

}
