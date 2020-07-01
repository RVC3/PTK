package ru.ppr.chit.ui.activity.setuser;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Dmitry Nevolin
 */
interface SetUserView extends MvpView {

    void setUserNameEmptyErrorVisible(boolean visible);
    void showError(String message);

}
