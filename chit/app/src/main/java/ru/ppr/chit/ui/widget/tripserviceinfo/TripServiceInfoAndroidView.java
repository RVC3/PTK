package ru.ppr.chit.ui.widget.tripserviceinfo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.core.ui.mvp.MvpDelegate;

/**
 * Отображает информацию об обслуживании поезда:
 * - номер поезда
 * - станция контроля
 * - имя пользователя (проводниа)
 * - станция отправления
 * - станция назначения
 *
 * @author Dmitry Nevolin
 */
public class TripServiceInfoAndroidView extends FrameLayout implements TripServiceInfoView {

    private static final SimpleDateFormat DEPARTURE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    // region Di
    private MvpDelegate mvpDelegate;
    private TripServiceInfoComponent component;
    private TripServiceInfoPresenter presenter;
    // endregion
    // region Views
    private TextView trainNumberAndDate;
    private TextView userNameView;
    private TextView controlStation;
    private TextView nextStation;
    private TextView boardingStatus;
    // endregion
    private String trainNumberValue;
    private String trainDepartureDateValue;

    public TripServiceInfoAndroidView(@NonNull Context context) {
        super(context);
        initialize();
    }

    public TripServiceInfoAndroidView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.widget_trip_service_info, this);

        if (isInEditMode()) {
            return;
        }

        mvpDelegate = new MvpDelegate(Dagger.appComponent().mvpProcessor(), this);
        component = DaggerTripServiceInfoComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .build();
        component.inject(this);

        trainNumberAndDate = (TextView) findViewById(R.id.trainNumberAndDate);
        userNameView = (TextView) findViewById(R.id.userName);
        controlStation = (TextView) findViewById(R.id.controlStation);
        nextStation = (TextView) findViewById(R.id.nextStation);
        boardingStatus = (TextView) findViewById(R.id.boardingStatus);
    }

    public void init(MvpDelegate parent, String id) {
        mvpDelegate.init(parent, id);
        presenter = mvpDelegate.getPresenter(component::tripServiceInfoPresenter, TripServiceInfoPresenter.class);
        presenter.initialize();
    }

    @Override
    public void setTrainNumber(@Nullable String trainNumber) {
        setTrainNumberAndDate(trainNumber, trainDepartureDateValue);
    }

    @Override
    public void setTrainDepartureDate(@Nullable Date trainDepartureDate) {
        setTrainNumberAndDate(trainNumberValue, trainDepartureDate == null ? null : DEPARTURE_DATE_FORMAT.format(trainDepartureDate));
    }

    @Override
    public void setUserName(@Nullable String userName) {
        userNameView.setText(userName);
    }

    @Override
    public void setControlStationName(@Nullable String stationName) {
        controlStation.setText(stationName);
    }

    @Override
    public void setNextStationName(@Nullable String stationName) {
        nextStation.setText(stationName);
    }

    @Override
    public void setBoardingStatus(@NonNull BoardingStatus boardingStatus) {
        String boardingStatusTitle;
        switch (boardingStatus) {
            case STARTED:
                boardingStatusTitle = getResources().getString(R.string.trip_service_info_boarding_status_started);
                break;
            case ENDED:
                boardingStatusTitle = getResources().getString(R.string.trip_service_info_boarding_status_ended);
                break;
            case UNKNOWN:
            default:
                boardingStatusTitle = getResources().getString(R.string.trip_service_info_boarding_status_unknown);
        }
        this.boardingStatus.setText(boardingStatusTitle);
    }

    private void setTrainNumberAndDate(@Nullable String trainNumber, @Nullable String trainDepartureDate) {
        this.trainNumberValue = trainNumber;
        this.trainDepartureDateValue = trainDepartureDate;
        if (trainNumber != null) {
            this.trainNumberAndDate.setText(getResources().getString(R.string.trip_service_info_train_number_value, trainNumberValue, trainDepartureDateValue));
            this.trainNumberAndDate.setTextColor(ContextCompat.getColor(getContext(), R.color.defaultGreen));
        } else {
            this.trainNumberAndDate.setText(R.string.trip_service_info_no_data);
            this.trainNumberAndDate.setTextColor(ContextCompat.getColor(getContext(), R.color.defaultRed));
        }
    }

}

