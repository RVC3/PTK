package ru.ppr.cppk.ui.fragment.exemptionManualInput;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class ExemptionManualInputViewState extends BaseMvpViewState<ExemptionManualInputView> implements ExemptionManualInputView {

    private boolean mReadFromCardBtnVisible = false;

    @Override
    protected void onViewAttached(ExemptionManualInputView view) {
        view.setReadFromCardBtnVisible(mReadFromCardBtnVisible);
    }

    @Override
    protected void onViewDetached(ExemptionManualInputView view) {

    }

    @Override
    public void setReadFromCardBtnVisible(boolean visible) {
        mReadFromCardBtnVisible = visible;
        forEachView(view -> view.setReadFromCardBtnVisible(mReadFromCardBtnVisible));
    }

    @Override
    public void showExemptionNotFoundMessage(int exemptionExpressCode) {
        forEachView(view -> view.showExemptionNotFoundMessage(exemptionExpressCode));
    }

    @Override
    public void showExemptionUsageDisabledMessage(ExemptionUsageDisabledMessage exemptionUsageDisabledMessage) {
        forEachView(view -> view.showExemptionUsageDisabledMessage(exemptionUsageDisabledMessage));
    }
}
