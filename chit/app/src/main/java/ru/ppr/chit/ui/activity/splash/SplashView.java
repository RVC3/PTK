package ru.ppr.chit.ui.activity.splash;


import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
public interface SplashView extends MvpView {

    void setState(State state);

    enum State{
        DEFAULT,
        INIT_EDS,
        INIT_EDS_ERRROR
    }
}
