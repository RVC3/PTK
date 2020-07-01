package ru.ppr.cppk.ui.activity.repealreadbsc;


import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class RepealReadBscViewState extends BaseMvpViewState<RepealReadBscView> implements RepealReadBscView {

    private int timerValue = 0;
    private State state = State.SEARCH_CARD;

    @Inject
    RepealReadBscViewState() {

    }

    @Override
    protected void onViewAttached(RepealReadBscView view) {
        view.setTimerValue(this.timerValue);
        view.setState(this.state);
    }

    @Override
    protected void onViewDetached(RepealReadBscView view) {

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
