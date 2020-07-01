package ru.ppr.chit.ui.activity.readbarcode;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Grigoriy Kashka
 */
class ReadBarcodeViewState extends BaseMvpViewState<ReadBarcodeView> implements ReadBarcodeView {

    private int timerValue = 0;
    private State state = State.SEARCH_BARCODE;

    @Inject
    ReadBarcodeViewState() {
    }

    @Override
    protected void onViewAttached(ReadBarcodeView view) {
        view.setTimerValue(this.timerValue);
        view.setState(this.state);
    }

    @Override
    protected void onViewDetached(ReadBarcodeView view) {

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