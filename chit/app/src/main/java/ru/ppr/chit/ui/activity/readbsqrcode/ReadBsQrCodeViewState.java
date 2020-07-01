package ru.ppr.chit.ui.activity.readbsqrcode;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class ReadBsQrCodeViewState extends BaseMvpViewState<ReadBsQrCodeView> implements ReadBsQrCodeView {

    private int timerValue = 0;
    private State state = State.SEARCH_BARCODE;

    @Inject
    ReadBsQrCodeViewState() {

    }

    @Override
    protected void onViewAttached(ReadBsQrCodeView view) {
        view.setTimerValue(this.timerValue);
        view.setState(this.state);
    }

    @Override
    protected void onViewDetached(ReadBsQrCodeView view) {

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
