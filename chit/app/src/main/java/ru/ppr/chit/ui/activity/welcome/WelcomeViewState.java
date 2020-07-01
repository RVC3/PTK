package ru.ppr.chit.ui.activity.welcome;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class WelcomeViewState extends BaseMvpViewState<WelcomeView> implements WelcomeView {

    private boolean startTripServiceVisible;

    @Inject
    WelcomeViewState() {

    }

    @Override
    protected void onViewAttached(WelcomeView view) {
        view.setStartTripServiceVisible(startTripServiceVisible);
    }

    @Override
    protected void onViewDetached(WelcomeView view) {

    }

    @Override
    public void setStartTripServiceVisible(boolean visible) {
        this.startTripServiceVisible = visible;
        forEachView(view -> view.setStartTripServiceVisible(this.startTripServiceVisible));
    }

}
