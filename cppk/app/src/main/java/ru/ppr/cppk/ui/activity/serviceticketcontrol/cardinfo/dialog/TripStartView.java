package ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo.dialog;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.nsi.entity.Station;

/**
 * @author Dmitry Nevolin
 */
interface TripStartView extends MvpView {

    void showLoadingDialog();

    void hideLoadingDialog();

    void setDepartureStationName(String departureStationName);

    void setDestinationStationName(String destinationStationName);

    void setDepartureStations(@NonNull List<Station> stations);

    void setContinueBtnEnable(boolean enable);
}
