package ru.ppr.core.ui.mvp.viewState;


import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * Состояние {@link MvpView}.
 *
 * @param <V> {@link MvpView}
 * @author Aleksandr Brazhkin
 */
public interface MvpViewState<V extends MvpView> {

    /**
     * Присоединение view.
     *
     * @param view Присоединенная view
     */
    void attachView(V view);

    /**
     * Отсоединение view.
     *
     * @param view Отсоединенная view
     */
    void detachView(V view);
}
