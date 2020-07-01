package ru.ppr.cppk.ui.activity.selectTransferStations;


import java.util.List;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.nsi.entity.Station;

/**
 * @author Grigoriy Kashka
 */
interface SelectTransferStationsView extends MvpView {

    void setDepartureStationName(String departureStationName);

    void setDestinationStationName(String destinationStationName);

    void setDepartureStations(List<Station> stations);

    void setDestinationStations(List<Station> stations);

    void showProgress();

    void hideProgress();

}
