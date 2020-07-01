package ru.ppr.chit.ui.activity.splash;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
public class SplashViewState extends BaseMvpViewState<SplashView> implements SplashView {

    private State state = State.DEFAULT;

    @Inject
    SplashViewState() {
    }

    @Override
    protected void onViewAttached(SplashView view) {
        view.setState(this.state);
    }

    @Override
    protected void onViewDetached(SplashView view) {

    }

    @Override
    public void setState(State state) {
        this.state = state;
        forEachView(view -> view.setState(this.state));
    }
}