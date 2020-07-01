package ru.ppr.cppk.ui.activity.root.ofdsettings;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Grigoriy Kashka
 */
interface OfdSettingsView extends MvpView {

    void setIp(String ip);

    void setPort(int port);

    void setTimeout(int timeout);

    void setState(State state);

    void showProgress();

    void hideProgress();

    enum State {
        DEFAULT,
        SUCCESS,
        ERROR_IP,
        ERROR_PORT,
        ERROR_TIMEOUT,
        ERROR_GET_DATA,
        ERROR_SET_DATA
    }
}
