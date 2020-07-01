package ru.ppr.cppk.ui.activity.pdrepeal;


import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface PdRepealView extends MvpView {
    void showAbortedState(String message);

    void hideAbortedState();

    void setInitializingStateVisible(boolean visible);
}
