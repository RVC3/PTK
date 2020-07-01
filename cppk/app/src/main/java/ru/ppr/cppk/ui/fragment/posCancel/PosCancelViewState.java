package ru.ppr.cppk.ui.fragment.posCancel;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class PosCancelViewState extends BaseMvpViewState<PosCancelView> implements PosCancelView {

    private Consumer<PosCancelView> mStateAction;

    @Override
    protected void onViewAttached(PosCancelView view) {
        if (mStateAction != null) {
            mStateAction.accept(view);
        }
    }

    @Override
    protected void onViewDetached(PosCancelView view) {

    }

    @Override
    public void showConnectingState(long timeout) {
        mStateAction = view -> view.showConnectingState(timeout);
        forEachView(mStateAction);
    }

    @Override
    public void showConnectionTimeoutState() {
        mStateAction = PosCancelView::showConnectionTimeoutState;
        forEachView(mStateAction);
    }

    @Override
    public void showConnectedState() {
        mStateAction = PosCancelView::showConnectedState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintingFirstSlip(boolean repeat) {
        mStateAction = view -> view.showPrintingFirstSlip(repeat);
        forEachView(mStateAction);
    }

    @Override
    public void showPrintFirstSlipFailState() {
        mStateAction = PosCancelView::showPrintFirstSlipFailState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintFirstSlipSuccessState() {
        mStateAction = PosCancelView::showPrintFirstSlipSuccessState;
        forEachView(mStateAction);
    }

    @Override
    public void showOperationRejectedWithoutSlip(String bankResponse) {
        mStateAction = view -> view.showOperationRejectedWithoutSlip(bankResponse);
        forEachView(mStateAction);
    }

    @Override
    public void showPrintingRejectSlipState(String bankResponse) {
        mStateAction = view -> view.showPrintingRejectSlipState(bankResponse);
        forEachView(mStateAction);
    }

    @Override
    public void showPrintRejectSlipSuccessState() {
        mStateAction = PosCancelView::showPrintRejectSlipFailState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintRejectSlipFailState() {
        mStateAction = PosCancelView::showPrintRejectSlipFailState;
        forEachView(mStateAction);
    }
}
