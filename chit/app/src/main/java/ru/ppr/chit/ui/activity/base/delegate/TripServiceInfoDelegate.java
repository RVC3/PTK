package ru.ppr.chit.ui.activity.base.delegate;

import android.app.Activity;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.ui.widget.tripserviceinfo.TripServiceInfoAndroidView;
import ru.ppr.core.ui.mvp.MvpDelegate;

/**
 * Делегат для добавления информации о поездке на экран.
 *
 * @author Aleksandr Brazhkin
 */
public class TripServiceInfoDelegate {

    private static final String MVP_TRIP_SERVICE_INFO_ID = "MVP_TRIP_SERVICE_INFO_ID";

    private final Activity activity;

    @Inject
    public TripServiceInfoDelegate(Activity activity) {
        this.activity = activity;
    }

    public void init(@NonNull MvpDelegate parent) {
        TripServiceInfoAndroidView tripServiceInfo = (TripServiceInfoAndroidView) activity.findViewById(R.id.trip_service_info);
        tripServiceInfo.init(parent, MVP_TRIP_SERVICE_INFO_ID);
    }
}
