package ru.ppr.cppk.ui.fragment.exemptionReadFromCard;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class ExemptionReadFromCardViewState extends BaseMvpViewState<ExemptionReadFromCardView> implements ExemptionReadFromCardView {

    boolean mCardNotFoundErrorShown;
    private boolean mReadCardErrorShown;
    private boolean mNoExemptionOnCardErrorShown;
    private String mCardInStopListErrorMsg;
    private boolean mCardValidityTimeErrorShown;
    private boolean mUnknownErrorShown;
    private String mBscType;
    private boolean mRetryBtnVisible;
    private String mBscNumber;
    private String mFio;
    private ExemptionInfo mExemptionInfo;
    private boolean mReadCardStateShown;
    private boolean mSearchCardStateShown;
    private boolean mReadCompletedStateShown;

    @Override
    protected void onViewAttached(ExemptionReadFromCardView view) {
        if (mCardNotFoundErrorShown) {
            view.showCardNotFoundError();
        }
        if (mReadCardErrorShown) {
            showReadCardError();
        }
        if (mNoExemptionOnCardErrorShown) {
            showNoExemptionOnCardError();
        }
        if (mCardInStopListErrorMsg != null) {
            showCardInStopListError(mCardInStopListErrorMsg);
        }
        if (mCardValidityTimeErrorShown) {
            showCardValidityTimeError();
        }
        if (mUnknownErrorShown) {
            showUnknownError();
        }
        view.setRetryBtnVisible(mRetryBtnVisible);
        view.setBscType(mBscType);
        view.setBscNumber(mBscNumber);
        view.setFio(mFio);
        view.setExemptionInfo(mExemptionInfo);
        if (mReadCardStateShown) {
            view.showReadCardState();
        }
        if (mSearchCardStateShown) {
            view.showSearchCardState();
        }
        if (mReadCompletedStateShown) {
            view.showReadCompletedState();
        }
    }

    @Override
    protected void onViewDetached(ExemptionReadFromCardView view) {

    }

    @Override
    public void showCardNotFoundError() {
        resetPreviousState();
        mCardNotFoundErrorShown = true;
        forEachView(view -> view.showCardNotFoundError());
    }

    @Override
    public void showReadCardError() {
        resetPreviousState();
        mReadCardErrorShown = true;
        forEachView(view -> view.showReadCardError());
    }

    @Override
    public void showNoExemptionOnCardError() {
        resetPreviousState();
        mNoExemptionOnCardErrorShown = true;
        forEachView(view -> view.showNoExemptionOnCardError());
    }

    @Override
    public void showCardInStopListError(String reason) {
        resetPreviousState();
        mCardInStopListErrorMsg = reason;
        forEachView(view -> view.showCardInStopListError(mCardInStopListErrorMsg));
    }

    @Override
    public void setRetryBtnVisible(boolean visible) {
        mRetryBtnVisible = true;
        forEachView(view -> view.setRetryBtnVisible(mRetryBtnVisible));
    }

    @Override
    public void showCardValidityTimeError() {
        resetPreviousState();
        mCardValidityTimeErrorShown = true;
        forEachView(view -> view.showCardValidityTimeError());
    }

    @Override
    public void setBscType(String bscType) {
        mBscType = bscType;
        forEachView(view -> view.setBscType(mBscType));
    }

    @Override
    public void setBscNumber(String bscNumber) {
        mBscNumber = bscNumber;
        forEachView(view -> view.setBscNumber(bscNumber));
    }

    @Override
    public void showUnknownError() {
        resetPreviousState();
        mUnknownErrorShown = true;
        forEachView(view -> view.showUnknownError());
    }

    @Override
    public void showReadCardState() {
        resetPreviousState();
        mReadCardStateShown = true;
        forEachView(view -> view.showReadCardState());
    }

    @Override
    public void showSearchCardState() {
        resetPreviousState();
        mSearchCardStateShown = true;
        forEachView(view -> view.showSearchCardState());
    }

    @Override
    public void showReadCompletedState() {
        resetPreviousState();
        mReadCompletedStateShown = true;
        forEachView(view -> view.showReadCompletedState());
    }

    @Override
    public void setTimerValue(String value) {
        forEachView(view -> view.setTimerValue(value));
    }

    @Override
    public void setFio(String fio) {
        mFio = fio;
        forEachView(view -> view.setFio(fio));
    }

    @Override
    public void setExemptionInfo(ExemptionInfo exemptionInfo) {
        mExemptionInfo = exemptionInfo;
        forEachView(view -> view.setExemptionInfo(exemptionInfo));
    }

    @Override
    public void showExemptionNotFoundMessage(int exemptionExpressCode) {
        forEachView(view -> view.showExemptionNotFoundMessage(exemptionExpressCode));
    }

    @Override
    public void showExemptionUsageDisabledMessage(ExemptionUsageDisabledMessage exemptionUsageDisabledMessage) {
        forEachView(view -> view.showExemptionUsageDisabledMessage(exemptionUsageDisabledMessage));
    }

    private void resetPreviousState(){
        mCardNotFoundErrorShown = false;
        mReadCardErrorShown = false;
        mNoExemptionOnCardErrorShown = false;
        mCardInStopListErrorMsg = null;
        mCardValidityTimeErrorShown = false;
        mUnknownErrorShown = false;
        mReadCardStateShown = false;
        mSearchCardStateShown = false;
        mReadCompletedStateShown = false;
    }
}
