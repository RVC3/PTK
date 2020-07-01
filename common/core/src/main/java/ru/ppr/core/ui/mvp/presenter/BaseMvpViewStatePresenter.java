package ru.ppr.core.ui.mvp.presenter;

import android.support.annotation.NonNull;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.core.ui.mvp.viewState.MvpViewState;


/**
 * Скелетная реализация {@link MvpPresenter, работающего с прослойкой {@link MvpViewState }.
 *
 * @param <V>  {@link MvpView }, c которой работает презентер.
 * @param <VS> {@link MvpViewState}, c которой работает презентер.
 * @author Aleksandr Brazhkin
 */
public abstract class BaseMvpViewStatePresenter<V extends MvpView, VS extends MvpViewState<V>> implements MvpPresenter<V> {

    /**
     * Представление, ассоциированное с презентером.
     */
    protected final V view;
    /**
     * {@link MvpViewState} прослойка
     */
    private final VS viewState;
    /**
     * Флаг, что презентер проинициализирован, т.е. начал свою работу
     * Удалить "2", когда не останется собственного флага в наследниках
     */
    private boolean initialized2 = false;

    /**
     * @deprecated Use {@link BaseMvpViewStatePresenter#BaseMvpViewStatePresenter(MvpViewState)} instead
     */
    @Deprecated
    public BaseMvpViewStatePresenter() {
        this.viewState = provideViewState();
        this.view = (V) viewState;
    }

    public BaseMvpViewStatePresenter(VS viewState) {
        this.viewState = viewState;
        this.view = (V) viewState;
    }

    /**
     * Удалить "2", когда не останется собственного флага в наследниках
     */
    public void initialize2() {
        if (!initialized2) {
            initialized2 = true;
            onInitialize2();
        }
    }

    /**
     * Удалить "2", когда не останется собственного флага в наследниках
     */
    protected void onInitialize2() {

    }

    public boolean isInitialized() {
        return initialized2;
    }

    /**
     * @deprecated Use {@link BaseMvpViewStatePresenter#BaseMvpViewStatePresenter(MvpViewState)} instead
     */
    @Deprecated
    protected VS provideViewState() {
        return null;
    }

    @Override
    public void bind(@NonNull V view) {
        viewState.attachView(view);
    }

    @Override
    public void unbind(@NonNull V view) {
        viewState.detachView(view);
    }

    @Override
    public void destroy() {

    }
}
