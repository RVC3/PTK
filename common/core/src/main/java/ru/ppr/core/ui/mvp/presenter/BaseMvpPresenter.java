package ru.ppr.core.ui.mvp.presenter;

import android.support.annotation.NonNull;

import ru.ppr.core.ui.mvp.view.MvpView;


/**
 * Скелетная реализация {@link MvpPresenter}.
 *
 * @param <V> {@link MvpView}, c которой работает презентер.
 * @author Aleksandr Brazhkin
 */

public abstract class BaseMvpPresenter<V extends MvpView> implements MvpPresenter<V> {

    /**
     * Представление, ассоциированное с презентером.
     */
    protected V view;

    @Override
    public void bind(@NonNull V view) {
        this.view = view;
    }

    @Override
    public void unbind(@NonNull V view) {
        this.view = null;
    }

    @Override
    public void destroy() {

    }
}
