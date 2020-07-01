package ru.ppr.cppk.ui.fragment.exemptionEnterSurname;


import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class ExemptionEnterSurnameViewState extends BaseMvpViewState<ExemptionEnterSurnameView> implements ExemptionEnterSurnameView {

    private boolean mSnilsFieldVisible;
    private boolean mIssueDateFieldVisible;
    private boolean mDocumentNumberFieldVisible;
    private ExemptionInfo mExemptionInfo;

    @Override
    protected void onViewAttached(ExemptionEnterSurnameView view) {
        view.setSnilsFieldVisible(mSnilsFieldVisible);
        view.setDocumentNumberFieldVisible(mDocumentNumberFieldVisible);
        view.setIssueDateFieldVisible(mIssueDateFieldVisible);
        view.setExemptionInfo(mExemptionInfo);
    }

    @Override
    protected void onViewDetached(ExemptionEnterSurnameView view) {

    }

    @Override
    public void showEmptyDocumentError(boolean isSnils) {
        forEachView(view -> view.showEmptyDocumentError(isSnils));
    }

    @Override
    public void showInvalidSnilsError() {
        forEachView(view -> view.showInvalidSnilsError());
    }

    @Override
    public void showInvalidFioError() {
        forEachView(view -> view.showInvalidFioError());
    }

    @Override
    public void showEmptyIssueDateError() {
        forEachView(view -> view.showEmptyIssueDateError());
    }

    @Override
    public void setSnilsFieldVisible(boolean visible) {
        mSnilsFieldVisible = visible;
        forEachView(view -> view.setSnilsFieldVisible(mSnilsFieldVisible));
    }

    @Override
    public void setIssueDateFieldVisible(boolean visible) {
        mIssueDateFieldVisible = visible;
        forEachView(view -> view.setIssueDateFieldVisible(visible));
    }

    @Override
    public void setDocumentNumberFieldVisible(boolean visible) {
        mDocumentNumberFieldVisible = visible;
        forEachView(view -> view.setDocumentNumberFieldVisible(visible));
    }

    @Override
    public void setExemptionInfo(ExemptionInfo exemptionInfo) {
        mExemptionInfo = exemptionInfo;
        forEachView(view -> view.setExemptionInfo(mExemptionInfo));
    }
}
