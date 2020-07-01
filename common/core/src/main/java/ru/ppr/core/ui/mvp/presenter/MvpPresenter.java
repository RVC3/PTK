package ru.ppr.core.ui.mvp.presenter;

import android.support.annotation.NonNull;

import ru.ppr.core.ui.mvp.view.MvpView;


/**
 * MVP презентер.
 *
 * @param <V> {@link MvpView}, c которой работает презентер.
 * @author Aleksandr Brazhkin
 */
public interface MvpPresenter<V extends MvpView> {
    /**
     * Привязывает {@link MvpView} к презентеру
     *
     * @param view Представление
     */
    void bind(@NonNull V view);

    /**
     * Отвязывает {@link MvpView} от презентера
     *
     * @param view
     */
    void unbind(@NonNull V view);

    /**
     * Вызывается при очищении ссылки на презентер.
     * Последняя точка, в которой нужно отвязаться от всех static ссылок.
     */
    void destroy();
}
