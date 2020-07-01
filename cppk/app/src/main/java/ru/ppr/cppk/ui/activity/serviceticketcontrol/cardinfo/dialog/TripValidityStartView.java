package ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo.dialog;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TicketType;

/**
 * @author Dmitry Nevolin
 */
class TripValidityStartView extends BaseMvpViewState<TripStartView> implements TripStartView {

    private boolean loadingDialogShown = false;
    private String departureStationName = "";
    private String destinationStationName = "";
    private List<Station> departureStations = Collections.emptyList();
    private boolean continueBtnEnable = false;

    @Inject
    TripValidityStartView() {

    }

    @Override
    protected void onViewAttached(TripStartView view) {
        if (loadingDialogShown) {
            view.showLoadingDialog();
        } else {
            view.hideLoadingDialog();
        }

        view.setDepartureStationName(departureStationName);
        view.setDestinationStationName(destinationStationName);
        view.setDepartureStations(departureStations);
        view.setContinueBtnEnable(continueBtnEnable);
    }

    @Override
    protected void onViewDetached(TripStartView view) {

    }

    @Override
    public void showLoadingDialog() {
        loadingDialogShown = true;
        forEachView(TripStartView::showLoadingDialog);
    }

    @Override
    public void hideLoadingDialog() {
        loadingDialogShown = false;
        forEachView(TripStartView::hideLoadingDialog);
    }

    @Override
    public void setDepartureStationName(String departureStationName) {
        this.departureStationName = departureStationName;
        forEachView(view -> view.setDepartureStationName(this.departureStationName));
    }

    @Override
    public void setDestinationStationName(String destinationStationName) {
        this.destinationStationName = destinationStationName;
        forEachView(view -> view.setDestinationStationName(this.destinationStationName));
    }

    @Override
    public void setDepartureStations(@NonNull List<Station> stations) {
        this.departureStations = stations;
        forEachView(view -> view.setDepartureStations(this.departureStations));
    }



    @Override
    public void setContinueBtnEnable(boolean enable) {
        this.continueBtnEnable = enable;
        forEachView(view -> view.setContinueBtnEnable(this.continueBtnEnable));
    }



}
