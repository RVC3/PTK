package ru.ppr.cppk.ui.activity.pdrepeal.poscancel;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class PosCancelViewState extends BaseMvpViewState<PosCancelView> implements PosCancelView {

    private Consumer<PosCancelView> mStateAction;

    @Inject
    PosCancelViewState() {

    }

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
}
