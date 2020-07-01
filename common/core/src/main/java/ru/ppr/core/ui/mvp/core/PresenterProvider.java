package ru.ppr.core.ui.mvp.core;


import ru.ppr.core.ui.mvp.presenter.MvpPresenter;

/**
 * Фабрика презентеров.
 *
 * @author Aleksandr Brazhkin
 */
public interface PresenterProvider<P extends MvpPresenter> {
    P providePresenter();
}
