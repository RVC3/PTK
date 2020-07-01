package ru.ppr.cppk.ui.fragment.pd.zoom;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.ui.fragment.pd.simple.model.BasePdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.CombinedCountTripsPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.CountTripsPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SeasonForDaysPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SeasonForPeriodPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.ServicePdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SinglePdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SurchargeSinglePdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.TicketPdViewModel;
import ru.ppr.cppk.ui.helper.DateListStringifier;

/**
 * Диалог с отображением информации о ПД в увеличенном виде.
 * S
 *
 * @author Aleksandr Brazhkin
 */
public class ZoomPdFragment extends DialogFragment {

    public static ZoomPdFragment newInstance() {
        return new ZoomPdFragment();
    }

    //region Di
    private ZoomPdComponent component;
    @Inject
    DateListStringifier dateListStringifier;
    //endregion
    //region Views
    private TextView pdTitle;
    private TextView validityLabel;
    private TextView pdNumber;
    private TextView errorDescription;

    private View stationsLayout;
    private TextView depStationName;
    private ImageView directionImage;
    private TextView destStationName;
    private TextView trainCategoryName;

    private View exemptionLayout;
    private TextView exemptionLabel;
    private TextView exemptionValue;

    private TextView pdDateTimeLabel;
    private TextView pdDateTimeValue;
    private TextView validityTimeLabel;
    private TextView validityFromDate;
    private TextView validityDash;
    private TextView validityToDate;

    private View classicLayout;

    private TextView availableTripsClassicLabel;
    private TextView availableTripsClassicCount;
    private TextView lastPassageClassicLabel;
    private TextView lastPassageTimeClassicView;
    private TextView noPassagesClassic;
    private TextView moreThanFourHoursClassic;

    private View combinedLayout;

    private TextView availableTrips6000Label;
    private TextView availableTrips6000Count;
    private TextView lastPassage6000Label;
    private TextView lastPassageTime6000View;
    private TextView noPassages6000;
    private TextView moreThanFourHours6000;

    private TextView availableTrips7000Label;
    private TextView availableTrips7000Count;
    private TextView lastPassage7000Label;
    private TextView lastPassageTime7000View;
    private TextView noPassages7000;
    private TextView moreThanFourHours7000;

    private TextView noPassageOnDepartureStation;
    private TextView wrongTrainCategoryOnDepartureStation;

    private Button closeBtn;
    //endregion
    //region Other
    private InteractionListener interactionListener;
    private BasePdViewModel pdViewModel;
    private boolean validityFromError;
    private boolean validityToError;
    //endregion

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        component = DaggerZoomPdComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), R.style.ZoomPdDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zoom_pd, container, false);

        pdTitle = (TextView) view.findViewById(R.id.pdTitle);
        validityLabel = (TextView) view.findViewById(R.id.validityLabel);
        pdNumber = (TextView) view.findViewById(R.id.pdNumber);
        errorDescription = (TextView) view.findViewById(R.id.errorDescription);

        stationsLayout = view.findViewById(R.id.stationsLayout);
        depStationName = (TextView) view.findViewById(R.id.departureStation);
        directionImage = (ImageView) view.findViewById(R.id.directionImage);
        destStationName = (TextView) view.findViewById(R.id.destinationStation);
        trainCategoryName = (TextView) view.findViewById(R.id.trainCategory);

        exemptionLayout = view.findViewById(R.id.exemptionLayout);
        exemptionLabel = (TextView) view.findViewById(R.id.exemptionLabel);
        exemptionValue = (TextView) view.findViewById(R.id.exemptionValue);

        pdDateTimeLabel = (TextView) view.findViewById(R.id.pdDateTimeLabel);
        pdDateTimeValue = (TextView) view.findViewById(R.id.pdDateTimeValue);
        validityTimeLabel = (TextView) view.findViewById(R.id.validityTimeLabel);
        validityFromDate = (TextView) view.findViewById(R.id.validityFromDate);
        validityDash = (TextView) view.findViewById(R.id.validityDash);
        validityToDate = (TextView) view.findViewById(R.id.validityToDate);

        classicLayout = view.findViewById(R.id.classicGroup);

        availableTripsClassicLabel = (TextView) view.findViewById(R.id.availableTripsClassicLabel);
        availableTripsClassicCount = (TextView) view.findViewById(R.id.availableTripsClassicCount);
        lastPassageClassicLabel = (TextView) view.findViewById(R.id.lastPassageClassicLabel);
        lastPassageTimeClassicView = (TextView) view.findViewById(R.id.lastPassageTimeClassicView);
        noPassagesClassic = (TextView) view.findViewById(R.id.noPassagesClassic);
        moreThanFourHoursClassic = (TextView) view.findViewById(R.id.moreThanFourHoursClassic);

        combinedLayout = view.findViewById(R.id.combinedGroup);

        availableTrips6000Label = (TextView) view.findViewById(R.id.availableTrips6000Label);
        availableTrips6000Count = (TextView) view.findViewById(R.id.availableTrips6000Count);
        lastPassage6000Label = (TextView) view.findViewById(R.id.lastPassage6000Label);
        lastPassageTime6000View = (TextView) view.findViewById(R.id.lastPassageTime6000View);
        noPassages6000 = (TextView) view.findViewById(R.id.noPassages6000);
        moreThanFourHours6000 = (TextView) view.findViewById(R.id.moreThanFourHours6000);

        availableTrips7000Label = (TextView) view.findViewById(R.id.availableTrips7000Label);
        availableTrips7000Count = (TextView) view.findViewById(R.id.availableTrips7000Count);
        lastPassage7000Label = (TextView) view.findViewById(R.id.lastPassage7000Label);
        lastPassageTime7000View = (TextView) view.findViewById(R.id.lastPassageTime7000View);
        noPassages7000 = (TextView) view.findViewById(R.id.noPassages7000);
        moreThanFourHours7000 = (TextView) view.findViewById(R.id.moreThanFourHours7000);

        noPassageOnDepartureStation = (TextView) view.findViewById(R.id.noPassageOnDepartureStation);
        wrongTrainCategoryOnDepartureStation = (TextView) view.findViewById(R.id.wrongTrainCategoryOnDepartureStation);

        closeBtn = (Button) view.findViewById(R.id.closeBtn);

        closeBtn.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Отрисовываем модель
        tryRenderModel();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (interactionListener != null) {
            interactionListener.onDismiss();
        }
    }

    public void setPdViewModel(@NonNull BasePdViewModel pdViewModel) {
        this.pdViewModel = pdViewModel;
        // Отрисовываем модель
        tryRenderModel();
    }

    private void tryRenderModel() {
        if (getView() == null || pdViewModel == null) {
            return;
        }
        renderPdViewModel(pdViewModel);
    }

    public void renderPdViewModel(BasePdViewModel pdViewModel) {
        renderBasePdViewModel(pdViewModel);
        if (pdViewModel instanceof TicketPdViewModel) {
            renderTicketPdViewModel((TicketPdViewModel) pdViewModel);
        }
        if (pdViewModel instanceof SinglePdViewModel) {
            renderSinglePdViewModel((SinglePdViewModel) pdViewModel);
            if (pdViewModel instanceof SurchargeSinglePdViewModel) {
                renderSurchargeSinglePdViewModel((SurchargeSinglePdViewModel) pdViewModel);
            }
        } else if (pdViewModel instanceof SeasonForPeriodPdViewModel) {
            renderSeasonForPeriodPdViewModel((SeasonForPeriodPdViewModel) pdViewModel);
        } else if (pdViewModel instanceof CountTripsPdViewModel) {
            renderCountTripsPdViewModel((CountTripsPdViewModel) pdViewModel);
        } else if (pdViewModel instanceof SeasonForDaysPdViewModel) {
            renderSeasonForDaysPdViewModel((SeasonForDaysPdViewModel) pdViewModel);
        } else if (pdViewModel instanceof ServicePdViewModel) {
            renderServicePdViewModel((ServicePdViewModel) pdViewModel);
        } else {
            throw new IllegalArgumentException("Unsupported pdViewModel");
        }
    }

    private void renderBasePdViewModel(BasePdViewModel pdViewModel) {
        // Устанавливаем тип ПД
        pdTitle.setText(pdViewModel.getTitle());
        // Устанавливаем номер ПД
        pdNumber.setText(getString(R.string.zoom_pd_pd_number, pdViewModel.getNumber()));
        // Отображаем валидность ПД
        setValid(pdViewModel.isValid());

        // Отображаем сообщение об ошибке, если нужно
        if (pdViewModel.isInvalidEdsKeyError()) {
            errorDescription.setVisibility(View.VISIBLE);
            errorDescription.setText(R.string.zoom_pd_error_invalid_eds_key);
        } else if (pdViewModel.isRevokedEdsKeyError()) {
            errorDescription.setVisibility(View.VISIBLE);
            errorDescription.setText(R.string.zoom_pd_error_revoked_eds_key);
        } else if (pdViewModel.isTicketInStopListError()) {
            errorDescription.setVisibility(View.VISIBLE);
            errorDescription.setText(R.string.zoom_pd_error_pd_in_stop_list);
        }
    }

    private void renderTicketPdViewModel(TicketPdViewModel pdViewModel) {
        // Устанавливаем станцию отправления
        if (pdViewModel.getDepStationName() == null) {
            depStationName.setText(getString(R.string.zoom_pd_pd_station_not_found));
        } else {
            depStationName.setText(pdViewModel.getDepStationName());
        }
        // Устанавливаем станцию назначения
        if (pdViewModel.getDestStationName() == null) {
            destStationName.setText(getString(R.string.zoom_pd_pd_station_not_found));
        } else {
            destStationName.setText(pdViewModel.getDestStationName());
        }
        // Устанавливаем льготу
        if (pdViewModel.getExemptionExpressCode() == 0) {
            exemptionValue.setText(R.string.zoom_pd_exemption_no_value);
        } else {
            exemptionValue.setText(String.valueOf(pdViewModel.getExemptionExpressCode()));
            exemptionValue.setTextColor(getResources().getColor((R.color.green)));
        }
        // Устанавливаем категорию поезда
        trainCategoryName.setText(pdViewModel.getTrainCategoryName());
        // Для трансфера скрываем информацию о льготе
        setExemptionLayoutVisible(!pdViewModel.isTransfer());
        // Для трансфера скрываем информацию о категории поезда
        setTrainCategoryLayoutVisible(!pdViewModel.isTransfer());
        // Отображаем блок со станциями
        setStationsLayoutVisible(true);
        // Отображаем валидность маршрута
        setRouteError(pdViewModel.isRouteError());
        // Отображаем валидность категории поезда
        setTrainCategoryError(pdViewModel.isTrainCategoryError());

        // Отображаем сообщение об ошибке, если нужно
        boolean errorDescriptionIsAlreadySet = pdViewModel.isInvalidEdsKeyError()
                || pdViewModel.isRevokedEdsKeyError()
                || pdViewModel.isTicketInStopListError();
        if (!errorDescriptionIsAlreadySet) {
            if (pdViewModel.isTicketAnnulledError()) {
                errorDescription.setVisibility(View.VISIBLE);
                errorDescription.setText(R.string.zoom_pd_error_pd_is_annulled);
            }
        }
    }

    private void renderSinglePdViewModel(SinglePdViewModel pdViewModel) {
        // Устанавливаем направление
        setDirection(pdViewModel.isTwoWay());
        // Отображаем блок с датой действия ПД
        setValidityDateLayoutVisible(true);
        // Скрываем блок с периодом действия ПД
        setValidityPeriodLayoutVisible(false);
        // Скрываем блок с информацией о количестве оставшихся поездок
        setCountTripLayoutVisible(false);
        // Устанавливаем дату действия ПД
        setValidityDate(pdViewModel.getValidityDate());
        // Отображаем валидность даты действия ПД
        setValidityDateError(pdViewModel.isValidityDateError());
    }

    private void renderSurchargeSinglePdViewModel(SurchargeSinglePdViewModel pdViewModel) {
        // Устанавливаем льготу (Переписываем значение после заполнения базовых полей)
        if (pdViewModel.getExemptionExpressCode() == 0) {
            // https://aj.srvdev.ru/browse/CPPKPP-28473
            // при чтении доплаты в поле "Льгота" выводить прочерк вместо слова "нет"
            exemptionValue.setText(R.string.simple_pd_no_exemption_for_fare_pd);
        }
        // Затираем категорию поезда и пишем туда номер родительского ПД
        trainCategoryName.setText(String.format(getString(R.string.fare_category), pdViewModel.getParentPdNumber()));
    }

    private void renderSeasonForPeriodPdViewModel(SeasonForPeriodPdViewModel pdViewModel) {
        // Устанавливаем направление
        setDirection(true);
        // Скрываем блок с датой действия ПД
        setValidityDateLayoutVisible(false);
        // Отображаем блок с периодом действия ПД
        setValidityPeriodLayoutVisible(true);
        // Скрываем блок с информацией о количестве оставшихся поездок
        setCountTripLayoutVisible(false);
        // Устанавливаем период действия ПД
        setFromDateTime(pdViewModel.getValidityFromDate());
        setToDateTime(pdViewModel.getValidityToDate());
        // Отображаем валидность периода действия ПД
        setValidityFromError(pdViewModel.isValidityFromDateError());
        setValidityToError(pdViewModel.isValidityToDateError());
        setWeekendOnlyError(pdViewModel.isWeekendOnlyError());
        setWorkingDayOnlyError(pdViewModel.isWorkingDayOnlyError());
    }

    private void renderCountTripsPdViewModel(CountTripsPdViewModel pdViewModel) {
        // Устанавливаем направление
        setDirection(true);
        // Скрываем блок с датой действия ПД
        setValidityDateLayoutVisible(false);
        // Отображаем блок с периодом действия ПД
        setValidityPeriodLayoutVisible(true);
        // Отображаем блок с информацией о количестве оставшихся поездок
        setCountTripLayoutVisible(true);
        // Устанавливаем период действия ПД
        setFromDateTime(pdViewModel.getValidityFromDate());
        setToDateTime(pdViewModel.getValidityToDate());
        // Отображаем валидность периода действия ПД
        setValidityFromError(pdViewModel.isValidityFromDateError());
        setValidityToError(pdViewModel.isValidityToDateError());

        if (!(pdViewModel instanceof CombinedCountTripsPdViewModel)) {
            // Скрываем ненужные слои для классических абонементов
            classicLayout.setVisibility(View.VISIBLE);
            combinedLayout.setVisibility(View.GONE);
            wrongTrainCategoryOnDepartureStation.setVisibility(View.GONE);

            // Устанавливаем количество оставшихся поездок
            availableTripsClassicCount.setText(String.valueOf(pdViewModel.getAvailableTripsCount()));
            // Устанавливаем наличие последнего прохода
            setLastPassageClassicTime(pdViewModel.getLastPassageTime(), pdViewModel.getMaxHoursAgo(), pdViewModel.isLastPassageError());
        }

        // Отображаем сообщение об отсутствии прохода
        if (pdViewModel.isLastPassageError()) {
            noPassageOnDepartureStation.setVisibility(View.VISIBLE);
        } else {
            noPassageOnDepartureStation.setVisibility(View.GONE);
        }
        // Отображаем факт отсутствия поездок
        if (!(pdViewModel instanceof CombinedCountTripsPdViewModel)) {
            setNoTripsClassicError(pdViewModel.isNoTripsError());
        }

        if (pdViewModel instanceof CombinedCountTripsPdViewModel) {
            setCombinedCountTripsPdViewModel((CombinedCountTripsPdViewModel) pdViewModel);
        }
    }

    void setCombinedCountTripsPdViewModel(CombinedCountTripsPdViewModel pdViewModel) {
        // Устанавливаем заголовок поля со временем прохода
        lastPassage6000Label.setText(R.string.count_trips_last_passage_6000);
        lastPassage7000Label.setText(R.string.count_trips_last_passage_7000);
        // Отображаем layout для комбинированного абонемента и скрываем старое(классическое) отображение
        combinedLayout.setVisibility(View.VISIBLE);
        classicLayout.setVisibility(View.GONE);
        // Устанавливаем количество оставшихся поездок
        availableTrips6000Count.setText(String.valueOf(pdViewModel.getAvailableTripsCount()));
        availableTrips7000Count.setText(String.valueOf(pdViewModel.getAvailableTripsCount7000()));
        // Устанавливаем наличие последнего прохода
        setLastPassage6000Time(pdViewModel.getLastPassageTime(), pdViewModel.getMaxHoursAgo(), pdViewModel.isLastPassageError());
        setLastPassage7000Time(pdViewModel.getLastPassageTime7000(), pdViewModel.getMaxHoursAgo(), pdViewModel.isLastPassage7000Error());
        // Отображаем сообщение об отсутствии прохода
        if (pdViewModel.isLastPassageError() || pdViewModel.isLastPassage7000Error()) {
            noPassageOnDepartureStation.setVisibility(View.VISIBLE);
        } else {
            noPassageOnDepartureStation.setVisibility(View.GONE);
        }

        if(pdViewModel.isWrongTrainCategory()) {
            wrongTrainCategoryOnDepartureStation.setVisibility(View.VISIBLE);
        } else {
            wrongTrainCategoryOnDepartureStation.setVisibility(View.GONE);
        }

        // Отображаем ошибки
        setNoTrips6000Error(pdViewModel.isNoTripsError());
        setNoTrips7000Error(pdViewModel.isNoTrips7000Error());
    }

    private void renderSeasonForDaysPdViewModel(SeasonForDaysPdViewModel pdViewModel) {
        // Устанавливаем направление
        setDirection(true);
        // Скрываем блок с периодом действия ПД
        setValidityPeriodLayoutVisible(false);
        // Отображаем блок с датой действия ПД
        setValidityDateLayoutVisible(true);
        // Скрываем блок с информацией о количестве оставшихся поездок
        setCountTripLayoutVisible(false);
        // Устанавливаем даты действия ПД
        setValidityDates(pdViewModel.getValidityDates());
        // Отображаем валидность дат действия ПД
        setValidityDateError(pdViewModel.isValidityDatesError());
    }

    private void renderServicePdViewModel(ServicePdViewModel pdViewModel) {
        // Скрываем блок со станциями
        setStationsLayoutVisible(false);
        // Скрываем блок с информацией о льготе
        setExemptionLayoutVisible(false);
        // Скрываем блок с информацией категории поезда
        setTrainCategoryLayoutVisible(false);
        // Скрываем блок с информацией о количестве оставшихся поездок
        setCountTripLayoutVisible(false);
    }

    private void setLastPassageClassicTime(@Nullable Date lastPassageTime, int maxHoursAgo, boolean error) {
        if (lastPassageTime == null) {
            lastPassageTimeClassicView.setText(R.string.count_trips_no_passages);
            moreThanFourHoursClassic.setVisibility(View.GONE);
            noPassagesClassic.setVisibility(error ? View.VISIBLE : View.GONE);
            lastPassageTimeClassicView.setVisibility(error ? View.GONE : View.VISIBLE);
        } else {
            moreThanFourHoursClassic.setText(getString(R.string.zoom_pd_more_n_hours, maxHoursAgo));
            noPassagesClassic.setVisibility(View.GONE);
            lastPassageTimeClassicView.setVisibility(View.VISIBLE);
            lastPassageTimeClassicView.setText(DateFormatOperations.getDateForOut(lastPassageTime));
            moreThanFourHoursClassic.setVisibility(error ? View.VISIBLE : View.GONE);
        }
    }

    private void setLastPassage6000Time(@Nullable Date lastPassageTime, int maxHoursAgo, boolean error) {
        if (lastPassageTime == null) {
            lastPassageTime6000View.setText(R.string.count_trips_no_passages);
            moreThanFourHours6000.setVisibility(View.GONE);
            noPassages6000.setVisibility(error ? View.VISIBLE : View.GONE);
            lastPassageTime6000View.setVisibility(error ? View.GONE : View.VISIBLE);
        } else {
            moreThanFourHours6000.setText(getString(R.string.zoom_pd_more_n_hours, maxHoursAgo));
            noPassages6000.setVisibility(View.GONE);
            lastPassageTime6000View.setVisibility(View.VISIBLE);
            lastPassageTime6000View.setText(DateFormatOperations.getDateForOut(lastPassageTime));
            moreThanFourHours6000.setVisibility(error ? View.VISIBLE : View.GONE);
        }
    }

    private void setLastPassage7000Time(@Nullable Date lastPassageTime, int maxHoursAgo, boolean error) {
        if (lastPassageTime == null) {
            lastPassageTime7000View.setText(R.string.count_trips_no_passages);
            moreThanFourHours7000.setVisibility(View.GONE);
            noPassages7000.setVisibility(error ? View.VISIBLE : View.GONE);
            lastPassageTime7000View.setVisibility(error ? View.GONE : View.VISIBLE);
        } else {
            moreThanFourHours7000.setText(getString(R.string.zoom_pd_more_n_hours, maxHoursAgo));
            noPassages7000.setVisibility(View.GONE);
            lastPassageTime7000View.setVisibility(View.VISIBLE);
            lastPassageTime7000View.setText(DateFormatOperations.getDateForOut(lastPassageTime));
            moreThanFourHours7000.setVisibility(error ? View.VISIBLE : View.GONE);
        }
    }

    private void setValid(boolean valid) {
        if (valid) {
            validityLabel.setBackgroundResource(R.color.zoom_pd_success);
            validityLabel.setText(R.string.zoom_pd_valid_pd);
        } else {
            validityLabel.setBackgroundResource(R.color.zoom_pd_error);
            validityLabel.setText(R.string.zoom_pd_invalid_pd);
        }
        pdTitle.setEnabled(valid);
        pdNumber.setEnabled(valid);
        depStationName.setEnabled(valid);
        destStationName.setEnabled(valid);
        trainCategoryName.setEnabled(valid);
        pdDateTimeLabel.setEnabled(valid);
        pdDateTimeValue.setEnabled(valid);

        validityTimeLabel.setEnabled(valid);
        validityFromDate.setEnabled(valid);
        validityDash.setEnabled(valid);
        validityToDate.setEnabled(valid);

        exemptionLabel.setEnabled(valid);
        exemptionValue.setEnabled(valid);

        availableTripsClassicLabel.setEnabled(valid);
        availableTripsClassicCount.setEnabled(valid);
        lastPassageClassicLabel.setEnabled(valid);
        lastPassageTimeClassicView.setEnabled(valid);

        availableTrips6000Label.setEnabled(valid);
        availableTrips6000Count.setEnabled(valid);
        lastPassage6000Label.setEnabled(valid);
        lastPassageTime6000View.setEnabled(valid);

        availableTrips7000Label.setEnabled(valid);
        availableTrips7000Count.setEnabled(valid);
        lastPassage7000Label.setEnabled(valid);
        lastPassageTime7000View.setEnabled(valid);
    }

    private void setValidityDateLayoutVisible(boolean visible) {
        pdDateTimeLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
        pdDateTimeValue.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setCountTripLayoutVisible(boolean visible) {
        if(visible) {
            classicLayout.setVisibility(View.VISIBLE);
            combinedLayout.setVisibility(View.GONE);
        } else {
            classicLayout.setVisibility(View.GONE);
            combinedLayout.setVisibility(View.GONE);
        }
    }

    private void setStationsLayoutVisible(boolean visible) {
        stationsLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setTrainCategoryLayoutVisible(boolean visible) {
        trainCategoryName.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setExemptionLayoutVisible(boolean visible) {
        exemptionLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setValidityPeriodLayoutVisible(boolean visible) {
        validityTimeLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
        validityFromDate.setVisibility(visible ? View.VISIBLE : View.GONE);
        validityDash.setVisibility(visible ? View.VISIBLE : View.GONE);
        validityToDate.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setValidityDate(Date validityDate) {
        if (validityDate == null) {
            pdDateTimeValue.setText(R.string.zoom_pd_date_time_no_value);
        } else {
            pdDateTimeValue.setText(DateFormatOperations.getDateddMMyyyy(validityDate));
        }
    }

    private void setValidityDates(List<Date> validityDates) {
        if (validityDates == null || validityDates.isEmpty()) {
            pdDateTimeValue.setText(R.string.zoom_pd_date_time_no_value);
        } else {
            pdDateTimeValue.setText(dateListStringifier.stringify(validityDates));
        }
    }

    private void setFromDateTime(Date fromDateTime) {
        if (fromDateTime == null) {
            validityFromDate.setText(R.string.zoom_pd_validity_time_from_no_value);
        } else {
            validityFromDate.setText(DateFormatOperations.getOutDate(fromDateTime));
        }
    }

    private void setToDateTime(Date toDateTime) {
        if (toDateTime == null) {
            validityToDate.setText(R.string.zoom_pd_validity_time_to_no_value);
        } else {
            validityToDate.setText(DateFormatOperations.getOutDate(toDateTime));
        }
    }

    private void setDirection(boolean twoWay) {
        if (twoWay) {
            directionImage.setImageResource(R.drawable.ic_direction_there_back);
        } else {
            directionImage.setImageResource(R.drawable.ic_direction_there);
        }
    }

    private void setTrainCategoryError(boolean error) {
        setDefaultTextViewError(trainCategoryName, error, true);
    }

    private void setRouteError(boolean error) {
        setDefaultTextViewError(depStationName, error, false);
        setDefaultTextViewError(destStationName, error, false);
    }

    private void setValidityDateError(boolean error) {
        setDefaultTextViewError(pdDateTimeLabel, error, true);
        setDefaultTextViewError(pdDateTimeValue, error, true);
    }

    private void setValidityFromError(boolean error) {
        this.validityFromError = error;
        setDefaultTextViewError(validityFromDate, error, true);
        updateValidityLabel();
    }

    private void setValidityToError(boolean error) {
        this.validityToError = error;
        setDefaultTextViewError(validityToDate, error, true);
        updateValidityLabel();
    }

    private void updateValidityLabel() {
        setDefaultTextViewError(validityTimeLabel, validityFromError || validityToError, true);
    }

    private void setNoTripsClassicError(boolean error) {
        setDefaultTextViewError(availableTripsClassicLabel, error, true);
        setDefaultTextViewError(availableTripsClassicCount, error, false);
    }

    private void setNoTrips6000Error(boolean error) {
        setDefaultTextViewError(availableTrips6000Label, error, true);
        setDefaultTextViewError(availableTrips6000Count, error, false);
    }

    private void setNoTrips7000Error(boolean error) {
        setDefaultTextViewError(availableTrips7000Label, error, true);
        setDefaultTextViewError(availableTrips7000Count, error, false);
    }

    private void setWeekendOnlyError(boolean weekendOnlyError) {
        setDefaultTextViewError(pdTitle, weekendOnlyError, false);
    }

    private void setWorkingDayOnlyError(boolean workingDayOnlyError) {
        setDefaultTextViewError(pdTitle, workingDayOnlyError, false);
    }

    private void setDefaultTextViewError(TextView textView, boolean error, boolean changeStyle) {
        if (error) {
            textView.setTextColor(getResources().getColor(R.color.zoom_pd_error));
        } else {
            textView.setTextColor(getResources().getColorStateList(R.color.zoom_pd_normal));
        }
        if (changeStyle) {
            textView.setTypeface(textView.getTypeface(), error ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void onDismiss();
    }
}
