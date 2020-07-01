package ru.ppr.chit.ui.widget.tripserviceinfo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Dmitry Nevolin
 */
interface TripServiceInfoView extends MvpView {

    void setTrainNumber(@Nullable String trainNumber);

    void setTrainDepartureDate(@Nullable Date trainDepartureDate);

    void setUserName(@Nullable String userName);

    void setControlStationName(@Nullable String stationName);

    void setNextStationName(@Nullable String stationName);

    void setBoardingStatus(@NonNull BoardingStatus boardingStatus);

    enum BoardingStatus {
        /**
         * Не можем определить статус, свойственно оффлайн-режиму
         */
        UNKNOWN,
        /**
         * Начато обслуживание поездки
         */
        STARTED,
        /**
         * Обслуживание поездки завершено
         */
        ENDED
    }
}
