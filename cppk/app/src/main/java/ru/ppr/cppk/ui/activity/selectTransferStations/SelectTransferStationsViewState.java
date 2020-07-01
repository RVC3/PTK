package ru.ppr.cppk.ui.activity.selectTransferStations;


import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;
import ru.ppr.nsi.entity.Station;

/**
 * @author Grigoriy Kashka
 */
class SelectTransferStationsViewState extends BaseMvpViewState<SelectTransferStationsView> implements SelectTransferStationsView {

    private boolean progressShown = false;
    private List<Station> mDepartureStations = Collections.emptyList();
    private List<Station> mDestinationStations = Collections.emptyList();
    private String mDepartureStationName = "";
    private String mDestinationStationName = "";

    @Inject
    SelectTransferStationsViewState(){

    }

    @Override
    protected void onViewAttached(SelectTransferStationsView view) {
        if (progressShown) {
            view.showProgress();
        } else {
            view.hideProgress();
        }
        view.setDepartureStations(mDepartureStations);
        view.setDestinationStations(mDestinationStations);
        view.setDepartureStationName(mDepartureStationName);
        view.setDestinationStationName(mDestinationStationName);
    }

    @Override
    protected void onViewDetached(SelectTransferStationsView view) {
    }

    @Override
    public void setDepartureStationName(String departureStationName) {
        mDepartureStationName = departureStationName;
        forEachView(view -> view.setDepartureStationName(mDepartureStationName));
    }

    @Override
    public void setDestinationStationName(String destinationStationName) {
        mDestinationStationName = destinationStationName;
        forEachView(view -> view.setDestinationStationName(mDestinationStationName));
    }

    @Override
    public void setDepartureStations(List<Station> stations) {
        mDepartureStations = stations;
        forEachView(view -> view.setDepartureStations(mDepartureStations));
    }

    @Override
    public void setDestinationStations(List<Station> stations) {
        mDestinationStations = stations;
        forEachView(view -> view.setDestinationStations(mDestinationStations));
    }

    @Override
    public void showProgress() {
        progressShown = true;
        forEachView(SelectTransferStationsView::showProgress);
    }

    @Override
    public void hideProgress() {
        progressShown = false;
        forEachView(SelectTransferStationsView::hideProgress);
    }
}
