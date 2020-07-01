package ru.ppr.cppk.ui.fragment.pd.servicefee;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.ui.fragment.pd.simple.model.ServicePdViewModel;

/**
 * @author Dmitry Nevolin
 */
class ServiceFeePdViewState extends BaseMvpViewState<ServiceFeePdView> implements ServiceFeePdView {

    private String pdTitle;
    private boolean isPdValid;
    private Integer pdNumber;
    private List<PassageResult> pdErrors = Collections.emptyList();
    private Date from;
    private Date to;
    private boolean serviceFeeNotFoundVisible;

    @Inject
    ServiceFeePdViewState() {
    }

    @Override
    protected void onViewAttached(ServiceFeePdView view) {
        view.updatePdTitle(pdTitle);
        view.updatePdValid(isPdValid);
        view.updatePdNumber(pdNumber);
        view.updatePdErrors(pdErrors);
        view.updateDateActionsFrom(from);
        view.updateDateActionsTo(to);
        view.updateServiceFeeNotFound(serviceFeeNotFoundVisible);
    }

    @Override
    protected void onViewDetached(ServiceFeePdView view) {

    }

    @Override
    public void updatePdTitle(@NonNull String pdTitle) {
        this.pdTitle = pdTitle;

        forEachView(view -> view.updatePdTitle(this.pdTitle));
    }

    @Override
    public void updatePdValid(boolean isPdValid) {
        this.isPdValid = isPdValid;

        forEachView(view -> view.updatePdValid(this.isPdValid));
    }

    @Override
    public void updatePdNumber(@NonNull Integer pdNumber) {
        this.pdNumber = pdNumber;

        forEachView(view -> view.updatePdNumber(this.pdNumber));
    }

    @Override
    public void updatePdErrors(@NonNull List<PassageResult> pdErrors) {
        this.pdErrors = pdErrors;

        forEachView(view -> view.updatePdErrors(this.pdErrors));
    }

    @Override
    public void updateDateActionsFrom(@NonNull Date from) {
        this.from = from;

        forEachView(view -> view.updateDateActionsFrom(this.from));
    }

    @Override
    public void updateDateActionsTo(@Nullable Date to) {
        this.to = to;

        forEachView(view -> view.updateDateActionsTo(this.to));
    }

    @Override
    public void updateServiceFeeNotFound(boolean visible) {
        this.serviceFeeNotFoundVisible = visible;

        forEachView(view -> view.updateServiceFeeNotFound(this.serviceFeeNotFoundVisible));
    }

    @Override
    public void showZoomPdDialog(ServicePdViewModel pdViewModel) {
        forEachView(view -> view.showZoomPdDialog(pdViewModel));
    }

}
