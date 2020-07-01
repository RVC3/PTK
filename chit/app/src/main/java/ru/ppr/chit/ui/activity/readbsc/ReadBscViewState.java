package ru.ppr.chit.ui.activity.readbsc;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
public class ReadBscViewState extends BaseMvpViewState<ReadBscView> implements ReadBscView {

    private int timerValue = 0;
    private State state = State.SEARCH_CARD;

    @Inject
    ReadBscViewState() {
    }

    @Override
    protected void onViewAttached(ReadBscView view) {
        view.setTimerValue(this.timerValue);
        view.setState(this.state);
    }

    @Override
    protected void onViewDetached(ReadBscView view) {

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