package ru.ppr.cppk.ui.activity.resultBarcodeCoupon;

import java.util.Date;

import ru.ppr.core.ui.mvp.view.MvpView;


/**
 * @author Dmitry Nevolin
 */
interface ResultBarcodeCouponView extends MvpView {

    void setValidityStatus(boolean valid);

    void setErrorMessage(ErrorMessage errorMessage);

    void setStationName(String name);

    void setStationValid(boolean valid);

    void setPrintDateTime(Date printDateTime);

    void setPrintDateTimeValid(boolean valid);

    void showMoreThanNHoursError(int hours);

    void showSalePdDisabledError();

    void hideSalePdDisabledError();

    void hideMoreThanNHoursError();

    void showProgress();

    void hideProgress();

    void showErrorTariffNotFound(String name);

    void setSalePdBtnVisible(boolean visible);

    void setBaggagePdBtnVisible(boolean visible);

    void setSalePdBtnEnabled(boolean enable);

    void setBaggagePdBtnEnabled(boolean enable);

    enum ErrorMessage {
        NONE,
        ALREADY_USED
    }

}
