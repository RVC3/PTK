package ru.ppr.cppk.ui.activity.resultBarcodeCoupon;

import java.util.Date;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;


/**
 * @author Dmitry Nevolin
 */
public class ResultBarcodeCouponViewState extends BaseMvpViewState<ResultBarcodeCouponView> implements ResultBarcodeCouponView {

    private boolean validityStatus;
    private ErrorMessage errorMessage = ErrorMessage.NONE;
    private String stationName;
    private boolean stationValid = true;
    private Date printDateTime = null;
    private boolean printDateTimeValid = true;
    private Integer moreThanNHoursError = null;
    private boolean salePdDisabledErrorVisible = false;
    private boolean progressShown = false;
    private boolean salePdBtnVisible = true;
    private boolean baggagePdBtnVisible = true;
    private boolean salePdBtnEnabled = true;
    private boolean baggagePdBtnEnabled = true;

    @Override
    protected void onViewAttached(ResultBarcodeCouponView view) {
        view.setValidityStatus(validityStatus);
        view.setErrorMessage(errorMessage);
        view.setStationName(stationName);
        view.setStationValid(stationValid);
        view.setPrintDateTime(printDateTime);
        view.setPrintDateTimeValid(printDateTimeValid);
        view.setSalePdBtnEnabled(salePdBtnEnabled);
        view.setBaggagePdBtnEnabled(baggagePdBtnEnabled);
        if (moreThanNHoursError == null) {
            view.hideMoreThanNHoursError();
        } else {
            view.showMoreThanNHoursError(moreThanNHoursError);
        }
        if (salePdDisabledErrorVisible) {
            view.showSalePdDisabledError();
        } else {
            view.hideSalePdDisabledError();
        }
        if (progressShown) {
            view.showProgress();
        } else {
            view.hideProgress();
        }
        view.setSalePdBtnVisible(salePdBtnVisible);
        view.setBaggagePdBtnVisible(baggagePdBtnVisible);
    }

    @Override
    protected void onViewDetached(ResultBarcodeCouponView view) {

    }

    @Override
    public void setValidityStatus(boolean valid) {
        this.validityStatus = valid;
        forEachView(view -> view.setValidityStatus(this.validityStatus));
    }

    @Override
    public void setErrorMessage(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
        forEachView(view -> view.setErrorMessage(this.errorMessage));
    }

    @Override
    public void setStationName(String name) {
        this.stationName = name;
        forEachView(view -> view.setStationName(this.stationName));
    }

    @Override
    public void setStationValid(boolean valid) {
        this.stationValid = valid;
        forEachView(view -> view.setStationValid(this.stationValid));
    }

    @Override
    public void setPrintDateTime(Date printDateTime) {
        this.printDateTime = printDateTime;
        forEachView(view -> view.setPrintDateTime(this.printDateTime));
    }

    @Override
    public void setPrintDateTimeValid(boolean valid) {
        this.printDateTimeValid = valid;
        forEachView(view -> view.setPrintDateTimeValid(this.printDateTimeValid));
    }

    @Override
    public void showMoreThanNHoursError(int hours) {
        this.moreThanNHoursError = hours;
        forEachView(view -> view.showMoreThanNHoursError(this.moreThanNHoursError));
    }

    @Override
    public void hideMoreThanNHoursError() {
        this.moreThanNHoursError = null;
        forEachView(ResultBarcodeCouponView::hideMoreThanNHoursError);
    }

    @Override
    public void showSalePdDisabledError() {
        this.salePdDisabledErrorVisible = true;
        forEachView(ResultBarcodeCouponView::showSalePdDisabledError);
    }

    @Override
    public void hideSalePdDisabledError() {
        this.salePdDisabledErrorVisible = false;
        forEachView(ResultBarcodeCouponView::hideSalePdDisabledError);
    }

    @Override
    public void showProgress() {
        this.progressShown = true;
        forEachView(ResultBarcodeCouponView::showProgress);
    }

    @Override
    public void hideProgress() {
        this.progressShown = false;
        forEachView(ResultBarcodeCouponView::hideProgress);
    }

    @Override
    public void showErrorTariffNotFound(String name) {
        forEachView(view -> view.showErrorTariffNotFound(name));
    }

    @Override
    public void setSalePdBtnVisible(boolean visible) {
        this.salePdBtnVisible = visible;
        forEachView(view -> view.setSalePdBtnVisible(salePdBtnVisible));
    }

    @Override
    public void setBaggagePdBtnVisible(boolean visible) {
        this.baggagePdBtnVisible = visible;
        forEachView(view -> view.setBaggagePdBtnVisible(baggagePdBtnVisible));
    }

    @Override
    public void setSalePdBtnEnabled(boolean enable) {
        this.salePdBtnEnabled = enable;
        forEachView(view -> view.setSalePdBtnEnabled(salePdBtnEnabled));
    }

    @Override
    public void setBaggagePdBtnEnabled(boolean enable) {
        this.baggagePdBtnEnabled = enable;
        forEachView(view -> view.setBaggagePdBtnEnabled(baggagePdBtnEnabled));
    }

}
