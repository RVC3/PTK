package ru.ppr.cppk.ui.activity.decrementtrip;


import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class DecrementTripViewState extends BaseMvpViewState<DecrementTripView> implements DecrementTripView {

    private int timerValue = 0;
    private State state = State.SEARCH_CARD;

    @Inject
    DecrementTripViewState() {
    }

    @Override
    protected void onViewAttached(DecrementTripView view) {
        view.setTimerValue(this.timerValue);
        view.setState(this.state);
    }

    @Override
    protected void onViewDetached(DecrementTripView view) {

    }

    @Override
    public void setTimerValue(int value) {
        this.timerValue = value;
        forEachView(view -> view.setTimerValue(this.timerValue));
    }

    @Override
    public void setState(State state) {
        this.state = state;
        forEachView(view -> view.setState(this.state));
    }
}
