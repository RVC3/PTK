package ru.ppr.chit.ui.widget.tripserviceinfo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class TripServiceInfoViewState extends BaseMvpViewState<TripServiceInfoView> implements TripServiceInfoView {

    private String trainNumber;
    private Date trainDepartureDate;
    private String userName;
    private String controlStationName;
    private String nextStationName;
    private BoardingStatus boardingStatus = BoardingStatus.UNKNOWN;

    @Inject
    TripServiceInfoViewState() {

    }

    @Override
    protected void onViewAttached(TripServiceInfoView view) {
        view.setTrainNumber(trainNumber);
        view.setTrainDepartureDate(trainDepartureDate);
        view.setUserName(userName);
        view.setControlStationName(controlStationName);
        view.setNextStationName(nextStationName);
        view.setBoardingStatus(boardingStatus);
    }

    @Override
    protected void onViewDetached(TripServiceInfoView view) {

    }

    @Override
    public void setTrainNumber(@Nullable String trainNumber) {
        this.trainNumber = trainNumber;
        forEachView(view -> view.setTrainNumber(this.trainNumber));
    }

    @Override
    public void setTrainDepartureDate(@Nullable Date trainDepartureDate) {
        this.trainDepartureDate = trainDepartureDate;
        forEachView(view -> view.setTrainDepartureDate(this.trainDepartureDate));
    }

    @Override
    public void setUserName(@Nullable String userName) {
        this.userName = userName;
        forEachView(view -> view.setUserName(this.userName));
    }

    @Override
    public void setControlStationName(@Nullable String stationName) {
        this.controlStationName = stationName;
        forEachView(view -> view.setControlStationName(this.controlStationName));
    }

    @Override
    public void setNextStationName(@Nullable String stationName) {
        this.nextStationName = stationName;
        forEachView(view -> view.setNextStationName(this.nextStationName));
    }

    @Override
    public void setBoardingStatus(@NonNull BoardingStatus boardingStatus) {
        this.boardingStatus = boardingStatus;
        forEachView(view -> view.setBoardingStatus(this.boardingStatus));
    }

}
