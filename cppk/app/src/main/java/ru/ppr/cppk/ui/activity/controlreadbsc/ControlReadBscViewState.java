package ru.ppr.cppk.ui.activity.controlreadbsc;


import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class ControlReadBscViewState extends BaseMvpViewState<ControlReadBscView> implements ControlReadBscView {

    private int timerValue = 0;
    private State state = State.SEARCH_CARD;
    private boolean newPdBtnVisible = false;

    @Inject
    ControlReadBscViewState() {
    }

    @Override
    protected void onViewAttached(ControlReadBscView view) {
        view.setTimerValue(this.timerValue);
        view.setSaleNewPdBtnVisible(this.newPdBtnVisible);
        view.setState(this.state);
    }

    @Override
    protected void onViewDetached(ControlReadBscView view) {

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

    @Override
    public void showSaleNewPdConfirmDialog() {
        forEachView(ControlReadBscView::showSaleNewPdConfirmDialog);
    }

    @Override
    public void setSaleNewPdBtnVisible(boolean visible) {
        this.newPdBtnVisible = visible;
        forEachView(view -> view.setSaleNewPdBtnVisible(this.newPdBtnVisible));
    }
}
