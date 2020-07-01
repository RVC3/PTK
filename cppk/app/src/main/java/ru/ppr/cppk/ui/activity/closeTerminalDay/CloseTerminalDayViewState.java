package ru.ppr.cppk.ui.activity.closeTerminalDay;


import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class CloseTerminalDayViewState extends BaseMvpViewState<CloseTerminalDayView> implements CloseTerminalDayView {

    private boolean mInitDayEndAskDialogShown = false;
    private boolean mPreparingDataProgressShown = false;
    private boolean mPreparingDataErrorShown = false;
    private boolean mClosingDayProgressShown = false;
    private boolean mClosingDayErrorShown = false;
    private String bankResponse = null;
    private Long mCloseDaySuccessDialogData = null;
    private boolean mPrintingSlipErrorShown = false;
    private boolean mPrintingSlipProgressShown = false;

    @Override
    protected void onViewAttached(CloseTerminalDayView view) {
        if (!mPreparingDataErrorShown && !mClosingDayErrorShown & !mPrintingSlipErrorShown) {
            hideAnyError();
        }
        if (mPreparingDataErrorShown) {
            view.showPreparingDataError();
        }
        if (mClosingDayErrorShown) {
            view.showClosingDayError(bankResponse);
        }
        if (mPrintingSlipErrorShown) {
            view.showPrintingSlipError();
        }
        if (mInitDayEndAskDialogShown) {
            view.showInitDayEndAskDialog();
        } else {
            view.hideInitDayEndAskDialog();
        }
        if (!mPreparingDataProgressShown && !mClosingDayProgressShown && !mPrintingSlipProgressShown) {
            hideAnyProgress();
        }
        if (mPreparingDataProgressShown) {
            view.showPreparingDataProgress();
        }
        if (mClosingDayProgressShown) {
            view.showClosingDayProgress();
        }
        if (mPrintingSlipProgressShown) {
            view.showPrintingSlipProgress();
        }
    }

    @Override
    protected void onViewDetached(CloseTerminalDayView view) {

    }

    @Override
    public void hideAnyError() {
        mPreparingDataErrorShown = false;
        mClosingDayErrorShown = false;
        mPrintingSlipErrorShown = false;
        for (CloseTerminalDayView view : views) {
            view.hideAnyError();
        }
    }

    @Override
    public void showInitDayEndAskDialog() {
        mInitDayEndAskDialogShown = true;
        for (CloseTerminalDayView view : views) {
            view.showInitDayEndAskDialog();
        }
    }

    @Override
    public void hideInitDayEndAskDialog() {
        mInitDayEndAskDialogShown = false;
        for (CloseTerminalDayView view : views) {
            view.hideInitDayEndAskDialog();
        }
    }

    @Override
    public void showPreparingDataProgress() {
        mPreparingDataProgressShown = true;
        for (CloseTerminalDayView view : views) {
            view.showPreparingDataProgress();
        }
    }

    @Override
    public void showPreparingDataError() {
        mPreparingDataErrorShown = true;
        for (CloseTerminalDayView view : views) {
            view.showPreparingDataError();
        }
    }

    @Override
    public void showClosingDayProgress() {
        mClosingDayProgressShown = true;
        for (CloseTerminalDayView view : views) {
            view.showClosingDayProgress();
        }
    }

    @Override
    public void showClosingDayError(String bankResponse) {
        mClosingDayErrorShown = true;
        this.bankResponse = bankResponse;
        for (CloseTerminalDayView view : views) {
            view.showClosingDayError(bankResponse);
        }
    }

    @Override
    public void showCloseDaySuccessDialog(long dayNumber) {
        mCloseDaySuccessDialogData = dayNumber;
        for (CloseTerminalDayView view : views) {
            view.showCloseDaySuccessDialog(mCloseDaySuccessDialogData);
        }
    }

    @Override
    public void hideAnyProgress() {
        mPreparingDataProgressShown = false;
        mClosingDayProgressShown = false;
        mPrintingSlipProgressShown = false;
        for (CloseTerminalDayView view : views) {
            view.hideAnyProgress();
        }
    }

    @Override
    public void showPrintingSlipProgress() {
        mPrintingSlipProgressShown = true;
        for (CloseTerminalDayView view : views) {
            view.showPrintingSlipProgress();
        }
    }

    @Override
    public void showPrintingSlipError() {
        mPrintingSlipErrorShown = true;
        for (CloseTerminalDayView view : views) {
            view.showPrintingSlipError();
        }
    }
}
