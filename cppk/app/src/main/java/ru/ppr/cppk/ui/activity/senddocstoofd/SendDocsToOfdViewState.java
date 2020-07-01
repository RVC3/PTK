package ru.ppr.cppk.ui.activity.senddocstoofd;


import java.util.Date;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class SendDocsToOfdViewState extends BaseMvpViewState<SendDocsToOfdView> implements SendDocsToOfdView {

    private int unsentDocsCount;
    private int firstUnsentDocNumber;
    private Date firstUnsentDocDateTime = null;
    private Error error = Error.NONE;
    private boolean progressVisible;

    @Inject
    SendDocsToOfdViewState() {

    }

    @Override
    protected void onViewAttached(SendDocsToOfdView view) {
        view.setUnsentDocsCount(this.unsentDocsCount);
        view.setFirstUnsentDocNumber(this.firstUnsentDocNumber);
        view.setFirstUnsentDocDateTime(this.firstUnsentDocDateTime);
        view.showError(this.error);
        if (progressVisible) {
            view.showProgress();
        } else {
            view.hideProgress();
        }
    }

    @Override
    protected void onViewDetached(SendDocsToOfdView view) {

    }

    @Override
    public void setUnsentDocsCount(int unsentDocsCount) {
        this.unsentDocsCount = unsentDocsCount;
        forEachView(view -> view.setUnsentDocsCount(this.unsentDocsCount));
    }

    @Override
    public void setFirstUnsentDocNumber(int firstUnsentDocNumber) {
        this.firstUnsentDocNumber = firstUnsentDocNumber;
        forEachView(view -> view.setFirstUnsentDocNumber(this.firstUnsentDocNumber));
    }

    @Override
    public void setFirstUnsentDocDateTime(Date firstUnsentDocDateTime) {
        this.firstUnsentDocDateTime = firstUnsentDocDateTime;
        forEachView(view -> view.setFirstUnsentDocDateTime(this.firstUnsentDocDateTime));
    }

    @Override
    public void showError(Error error) {
        this.error = error;
        forEachView(view -> view.showError(this.error));
    }

    @Override
    public void showProgress() {
        this.progressVisible = true;
        forEachView(SendDocsToOfdView::showProgress);
    }

    @Override
    public void hideProgress() {
        this.progressVisible = false;
        forEachView(SendDocsToOfdView::hideProgress);
    }
}
