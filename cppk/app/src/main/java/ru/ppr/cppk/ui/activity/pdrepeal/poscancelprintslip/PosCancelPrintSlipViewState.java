package ru.ppr.cppk.ui.activity.pdrepeal.poscancelprintslip;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class PosCancelPrintSlipViewState extends BaseMvpViewState<PosCancelPrintSlipView> implements PosCancelPrintSlipView {

    private Consumer<PosCancelPrintSlipView> mStateAction;

    @Inject
    PosCancelPrintSlipViewState() {

    }

    @Override
    protected void onViewAttached(PosCancelPrintSlipView view) {
        if (mStateAction != null) {
            mStateAction.accept(view);
        }
    }

    @Override
    protected void onViewDetached(PosCancelPrintSlipView view) {

    }

    @Override
    public void showPrintingState() {
        mStateAction = PosCancelPrintSlipView::showPrintingState;
        forEachView(mStateAction);
    }

    @Override
    public void showFailState() {
        mStateAction = PosCancelPrintSlipView::showFailState;
        forEachView(mStateAction);
    }

    @Override
    public void showSuccessState() {
        mStateAction = PosCancelPrintSlipView::showSuccessState;
        forEachView(mStateAction);
    }
}
