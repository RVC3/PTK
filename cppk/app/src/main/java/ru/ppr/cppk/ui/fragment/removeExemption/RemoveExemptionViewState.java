package ru.ppr.cppk.ui.fragment.removeExemption;


import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class RemoveExemptionViewState extends BaseMvpViewState<RemoveExemptionView> implements RemoveExemptionView {

    private ExemptionInfo mExemptionInfo;
    private boolean isSnilsFieldVisible;
    private boolean isDocumentNumberFieldVisible;

    @Override
    protected void onViewAttached(RemoveExemptionView view) {
        view.setExemptionInfo(mExemptionInfo);
        view.setSnilsFieldVisible(isSnilsFieldVisible);
        view.setDocumentNumberFieldVisible(isDocumentNumberFieldVisible);
    }

    @Override
    protected void onViewDetached(RemoveExemptionView view) {

    }

    @Override
    public void setExemptionInfo(ExemptionInfo exemptionInfo) {
        mExemptionInfo = exemptionInfo;
        forEachView(view -> view.setExemptionInfo(mExemptionInfo));
    }

    @Override
    public void setSnilsFieldVisible(boolean visible) {
        isSnilsFieldVisible = visible;
        forEachView(view -> view.setSnilsFieldVisible(isSnilsFieldVisible));
    }

    @Override
    public void setDocumentNumberFieldVisible(boolean visible) {
        isDocumentNumberFieldVisible = visible;
        forEachView(view -> view.setDocumentNumberFieldVisible(isDocumentNumberFieldVisible));
    }

}
