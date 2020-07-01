package ru.ppr.cppk.ui.fragment.pd.countrips;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.decrementtrip.DecrementTripActivity;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripParams;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripResult;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.pd.countrips.model.CountTripsPdViewParams;
import ru.ppr.cppk.ui.fragment.pd.simple.model.CombinedCountTripsPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.CountTripsPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.zoom.ZoomPdFragment;
import ru.ppr.logger.Logger;

/**
 * Фрагмент для отображения абонемента на количество поездок
 */
public class CountTripsFragment extends FragmentParent {

    private static final String TAG = Logger.makeLogTag(CountTripsFragment.class);

    // RC
    private static final int RC_DECREMENT_TRIP = 101;

    public static CountTripsFragment newInstance() {
        return new CountTripsFragment();
    }

    //region Views
    private TextView pdTitle;
    private TextView validityLabel;
    private TextView pdNumber;
    private TextView errorDescription;

    private TextView departureStation;
    private TextView destinationStation;
    private TextView trainCategory;

    private View exemptionLayout;
    private View exemptionLabel;
    private View exemptionValue;

    private TextView validityTimeLabel;
    private TextView validityFromLabel;
    private TextView validityFromDate;
    private TextView validityFromDash;
    private TextView validityToLabel;
    private TextView validityToDate;

    private View classicLayout;

    private TextView availableTripsClassicLabel;
    private TextView availableTripsClassicCount;
    private TextView lastPassageClassicLabel;
    private TextView lastPassageTimeClassicView;
    private TextView noPassagesClassic;
    private TextView moreThanFourHoursClassic;

    private View combinedLayout;
    private View availableTripsCombinedLabel;
    private View lastPassageCombinedLabel;

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

    private Button decrementTripBtn;
    private Button pdNotValidBtn;

    //endregion

    private boolean validityFromError;
    private boolean validityToError;

    private CountTripsPdViewParams countTripsPdViewParams;
    private Callback callback;
    /**
     * Кнопка: "Оформить билет по доплате"
     */
    private Button saleWithSurchargeBtn = null;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (countTripsPdViewParams.isInvalidData()) {
            Logger.info(TAG, "Неверные входные данные");
            View v = inflater.inflate(R.layout.fragment_error_tariff, null);
            ((TextView) v.findViewById(R.id.error_fragment_message)).setText(R.string.incorrect_data);
            v.findViewById(R.id.circleImage).setVisibility(View.VISIBLE);
            return v;
        }

        View view;
        if (countTripsPdViewParams.isSmallSize()) {
            view = inflater.inflate(R.layout.count_trips_small_fragment, container, false);
            view.setOnClickListener(v -> onZoomPdClicked());
        } else {
            view = inflater.inflate(R.layout.count_trips_big_fragment, container, false);
        }

        pdTitle = (TextView) view.findViewById(R.id.pdTitle);
        validityLabel = (TextView) view.findViewById(R.id.validityLabel);
        pdNumber = (TextView) view.findViewById(R.id.pdNumber);
        errorDescription = (TextView) view.findViewById(R.id.errorDescription);

        departureStation = (TextView) view.findViewById(R.id.departureStation);
        destinationStation = (TextView) view.findViewById(R.id.destinationStation);
        trainCategory = (TextView) view.findViewById(R.id.trainCategory);

        exemptionLayout = view.findViewById(R.id.exemptionLayout);
        exemptionLabel = view.findViewById(R.id.exemptionLabel);
        exemptionValue = view.findViewById(R.id.exemptionValue);

        validityTimeLabel = (TextView) view.findViewById(R.id.validityTimeLabel);
        validityFromLabel = (TextView) view.findViewById(R.id.validityFromLabel);
        validityFromDate = (TextView) view.findViewById(R.id.validityFromDate);
        validityFromDash = (TextView) view.findViewById(R.id.validityFromDash);
        validityToLabel = (TextView) view.findViewById(R.id.validityToLabel);
        validityToDate = (TextView) view.findViewById(R.id.validityToDate);

        classicLayout = view.findViewById(R.id.classicGroup);

        availableTripsClassicLabel = (TextView) view.findViewById(R.id.availableTripsClassicLabel);
        availableTripsClassicCount = (TextView) view.findViewById(R.id.availableTripsClassicCount);
        lastPassageClassicLabel = (TextView) view.findViewById(R.id.lastPassageClassicLabel);
        lastPassageTimeClassicView = (TextView) view.findViewById(R.id.lastPassageTimeClassicView);
        noPassagesClassic = (TextView) view.findViewById(R.id.noPassagesClassic);
        moreThanFourHoursClassic = (TextView) view.findViewById(R.id.moreThanFourHoursClassic);

        combinedLayout = view.findViewById(R.id.combinedGroup);
        availableTripsCombinedLabel = view.findViewById(R.id.availableTripsCombinedLabel);
        lastPassageCombinedLabel = view.findViewById(R.id.lastPassageCombinedLabel);

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

        // Настраиваем кнопку "Билет НЕдействителен"
        pdNotValidBtn = (Button) view.findViewById(R.id.pdNotValidBtn);
        pdNotValidBtn.setOnClickListener(v -> onPdNotValidBtnClicked());
        // Настраиваем кнопку "Списать поездку"
        decrementTripBtn = (Button) view.findViewById(R.id.decrementTripBtn);
        decrementTripBtn.setOnClickListener(v -> onDecrementTripBtnClicked());
        // Настраиваем кнопку "Оформить билет по доплате"
        saleWithSurchargeBtn = (Button) view.findViewById(R.id.saleWithSurchargeBtn);
        saleWithSurchargeBtn.setOnClickListener(v -> onSellWithExtraPaymentBtnClicked());

        // Отображаем модель
        setPdViewModel(countTripsPdViewParams.getPdViewModel());
        // Настраиваем видимость кнопок
        setButtons(countTripsPdViewParams);

        return view;
    }

    private void setButtons(CountTripsPdViewParams params) {
        pdNotValidBtn.setVisibility(params.isTicketNotValidBtnVisible() ? View.VISIBLE : View.GONE);
        saleWithSurchargeBtn.setVisibility(params.isSellSurchargeBtnVisible() ? View.VISIBLE : View.GONE);
        decrementTripBtn.setVisibility(params.isDecrementTripBtnVisible() ? View.VISIBLE : View.GONE);
        if (params.getPdViewModel() instanceof CombinedCountTripsPdViewModel) {
            // Это комбинированный абонемент
            if (params.isDecrement7000()) {
                decrementTripBtn.setText(R.string.count_trips_decrement_trip_7000_btn);
            } else if (params.isFixPassageMark()) {
                decrementTripBtn.setText(R.string.count_trips_fix_passage_mark_btn);
            } else {
                decrementTripBtn.setText(R.string.count_trips_decrement_trip_btn);
            }
        } else {
            // Классический сценарий
            decrementTripBtn.setText(R.string.count_trips_decrement_trip_btn);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setCountTripsPdViewParams(CountTripsPdViewParams countTripsPdViewParams) {
        this.countTripsPdViewParams = countTripsPdViewParams;
        if (getView() != null) {
            setPdViewModel(countTripsPdViewParams.getPdViewModel());
            setButtons(countTripsPdViewParams);
        }
    }

    private void setPdViewModel(CountTripsPdViewModel pdViewModel) {
        setCountTripsPdViewModel(pdViewModel);
        if (pdViewModel instanceof CombinedCountTripsPdViewModel) {
            setCombinedCountTripsPdViewModel((CombinedCountTripsPdViewModel) pdViewModel);
        }
    }

    private void setCountTripsPdViewModel(CountTripsPdViewModel pdViewModel) {
        // Устанавливаем номер ПД
        setPdNumber(pdViewModel.getNumber());
        // Устанавливаем станцию отправления
        setDepartureStationName(pdViewModel.getDepStationName());
        // Устанавливаем станцию назначения
        setDestinationStationName(pdViewModel.getDestStationName());
        // Устанавливаем название ПД
        setPdTitle(pdViewModel.getTitle());
        // Устанавливаем дату начала действия
        setValidityFromDate(pdViewModel.getValidityFromDate());
        // Устанавливаем дату окончания действия
        setValidityToDate(pdViewModel.getValidityToDate());
        // Устанавливаем категорию поезда для ПД
        setTrainCategory(pdViewModel.getTrainCategoryName());

        if (!(pdViewModel instanceof CombinedCountTripsPdViewModel)) {
            // Скрываем ненужные слои для классических абонементов
            classicLayout.setVisibility(View.VISIBLE);
            combinedLayout.setVisibility(View.GONE);
            wrongTrainCategoryOnDepartureStation.setVisibility(View.GONE);
            // Устанавливаем количество оставшихся поездок
            setAvailableTripsClassicCount(pdViewModel.getAvailableTripsCount());
            // Устанавливаем наличие последнего прохода
            setLastPassageClassicTime(pdViewModel.getLastPassageTime(), pdViewModel.getMaxHoursAgo(), pdViewModel.isLastPassageError());
        }
        // Отображаем сообщение об отсутствии прохода
        if (pdViewModel.isLastPassageError()) {
            noPassageOnDepartureStation.setVisibility(View.VISIBLE);
        } else {
            noPassageOnDepartureStation.setVisibility(View.GONE);
        }

        // Скрываем информацию о льготе для ПД трансфера
        // Хоть для поезда это поле все равно никогда не заполняется, оно видно для унификации вывода ПД на ЖД
        // http://agile.srvdev.ru/browse/CPPKPP-36166
        setExemptionVisible(!pdViewModel.isTransfer());
        // Настраиваем видимость категории поезда
        setTrainCategoryVisible(!pdViewModel.isTransfer());

        // Отображаем ошибки
        setValid(pdViewModel.isValid());
        setTrainCategoryError(pdViewModel.isTrainCategoryError());
        setStationsError(pdViewModel.isRouteError());
        setValidityFromError(pdViewModel.isValidityFromDateError());
        setValidityToError(pdViewModel.isValidityToDateError());

        if (!(pdViewModel instanceof CombinedCountTripsPdViewModel)) {
            setNoTripsClassicError(pdViewModel.isNoTripsError());
        }

        if (pdViewModel.isInvalidEdsKeyError()) {
            errorDescription.setVisibility(View.VISIBLE);
            errorDescription.setText(R.string.count_trips_error_invalid_eds_key);
            pdViewModel.setInvalidEdsKeyError(true);
        } else if (pdViewModel.isRevokedEdsKeyError()) {
            errorDescription.setVisibility(View.VISIBLE);
            errorDescription.setText(R.string.count_trips_error_revoked_eds_key);
            pdViewModel.setRevokedEdsKeyError(true);
        } else if (pdViewModel.isTicketInStopListError()) {
            errorDescription.setVisibility(View.VISIBLE);
            errorDescription.setText(R.string.count_trips_error_pd_in_stop_list);
            pdViewModel.setTicketInStopListError(true);
        } else {
            errorDescription.setVisibility(View.GONE);
        }
    }

    void setCombinedCountTripsPdViewModel(CombinedCountTripsPdViewModel pdViewModel) {
        // Устанавливаем заголовок поля со временем прохода
        lastPassage6000Label.setText(R.string.count_trips_last_passage_6000);
        // Отображаем layout для комбинированного абонемента и скрываем старое(классическое) отображение
        combinedLayout.setVisibility(View.VISIBLE);
        classicLayout.setVisibility(View.GONE);

        // Устанавливаем количество оставшихся поездок
        setAvailableTrips6000Count(pdViewModel.getAvailableTripsCount6000());
        int availableTrips7000CountValue = pdViewModel.getAvailableTripsCount7000();
        setAvailableTrips7000Count(availableTrips7000CountValue);
        // Устанавливаем наличие последнего прохода
        setLastPassage6000Time(pdViewModel.getLastPassageTime(), pdViewModel.getMaxHoursAgo(), pdViewModel.isLastPassageError());
        setLastPassage7000Time(pdViewModel.getLastPassageTime7000(), pdViewModel.getMaxHoursAgo(), pdViewModel.isLastPassage7000Error());
        // Отображаем сообщение об отсутствии прохода
        if (pdViewModel.isLastPassageError() || (pdViewModel.isLastPassage7000Error() && !pdViewModel.isWrongTrainCategory())) {
            noPassageOnDepartureStation.setVisibility(View.VISIBLE);
        } else {
            noPassageOnDepartureStation.setVisibility(View.GONE);
        }

        if (pdViewModel.isWrongTrainCategory()) {
            wrongTrainCategoryOnDepartureStation.setVisibility(View.VISIBLE);
        } else {
            wrongTrainCategoryOnDepartureStation.setVisibility(View.GONE);
        }

        // Отображаем ошибки
        setNoTrips6000Error(pdViewModel.isNoTripsError());
        setNoTrips7000Error(pdViewModel.isNoTrips7000Error());

        if (pdViewModel.isWrongTrainCategory() && availableTrips7000CountValue == 0) {
            highlightTextView(availableTrips7000Count);
        }
    }

    //region View methods

    private void setAvailableTripsClassicCount(int count) {
        availableTripsClassicCount.setText(String.valueOf(count));
    }

    private void setAvailableTrips6000Count(int count) {
        availableTrips6000Count.setText(String.valueOf(count));
    }

    private void setAvailableTrips7000Count(int count) {
        availableTrips7000Count.setText(String.valueOf(count));
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
            validityLabel.setBackgroundResource(R.color.count_trips_success);
            validityLabel.setText(R.string.count_trips_valid_pd);
        } else {
            validityLabel.setBackgroundResource(R.color.count_trips_error);
            validityLabel.setText(R.string.count_trips_invalid_pd);
        }
        pdTitle.setEnabled(valid);
        pdNumber.setEnabled(valid);
        departureStation.setEnabled(valid);
        destinationStation.setEnabled(valid);
        trainCategory.setEnabled(valid);
        validityTimeLabel.setEnabled(valid);
        if (validityFromLabel != null) {
            validityFromLabel.setEnabled(valid);
        }
        validityFromDate.setEnabled(valid);
        if (validityFromDash != null) {
            validityFromDash.setEnabled(valid);
        }
        if (validityToLabel != null) {
            validityToLabel.setEnabled(valid);
        }
        validityToDate.setEnabled(valid);

        availableTripsCombinedLabel.setEnabled(valid);
        lastPassageCombinedLabel.setEnabled(valid);

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

        exemptionLabel.setEnabled(valid);
        exemptionValue.setEnabled(valid);
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

    private void setPdNumber(int number) {
        pdNumber.setText(getString(R.string.number_for_pd, number));
    }

    private void setPdTitle(String title) {
        pdTitle.setText(title);
    }

    private void setValidityFromDate(Date date) {
        if (date == null) {
            validityFromDate.setText(R.string.not_found);
        } else {
            validityFromDate.setText(DateFormatOperations.getOutDate(date));
        }
    }

    public void setValidityToDate(Date date) {
        if (date == null) {
            validityToDate.setText(R.string.not_found);
        } else {
            validityToDate.setText(DateFormatOperations.getOutDate(date));
        }
    }

    private void setTrainCategory(String category) {
        trainCategory.setText(category);
    }

    private void setDepartureStationName(String name) {
        if (name == null) {
            departureStation.setText(R.string.not_found);
        } else {
            departureStation.setText(name);
        }
    }

    private void setDestinationStationName(String name) {
        if (name == null) {
            destinationStation.setText(R.string.not_found);
        } else {
            destinationStation.setText(name);
        }
    }

    private void setTrainCategoryError(boolean error) {
        setDefaultTextViewError(trainCategory, error, true);
    }

    private void setStationsError(boolean error) {
        setDefaultTextViewError(departureStation, error, false);
        setDefaultTextViewError(destinationStation, error, false);
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

    private void setDefaultTextViewError(TextView textView, boolean error, boolean changeStyle) {
        if (error) {
            textView.setTextColor(getResources().getColor(R.color.count_trips_error));
        } else {
            textView.setTextColor(getResources().getColorStateList(R.color.count_trips_normal));
        }
        if (changeStyle) {
            textView.setTypeface(textView.getTypeface(), error ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void highlightTextView(TextView textView) {
        textView.setBackgroundColor(getResources().getColor(R.color.count_trips_error));
        textView.setTextColor(getResources().getColor(R.color.white));
    }

    private void setExemptionVisible(boolean visible) {
        exemptionLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setTrainCategoryVisible(boolean visible) {
        trainCategory.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    //endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_DECREMENT_TRIP:
                DecrementTripResult decrementTripResult = DecrementTripActivity.getResultFromIntent(resultCode, data);
                callback.onDecrementTripResultReturned(decrementTripResult);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onZoomPdClicked() {
        callback.onZoomDialogShown();
        ZoomPdFragment zoomPdFragment = ZoomPdFragment.newInstance();
        zoomPdFragment.setCancelable(true);
        zoomPdFragment.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        zoomPdFragment.setPdViewModel(countTripsPdViewParams.getPdViewModel());
        zoomPdFragment.setInteractionListener(() -> callback.onZoomDialogHidden());
    }

    private void onPdNotValidBtnClicked() {
        Logger.info(TAG, "onPdNotValidBtnClicked");
        callback.onPdNotValidBtnClicked();
    }

    private void onSellWithExtraPaymentBtnClicked() {
        Logger.info(TAG, "onSellWithExtraPaymentBtnClicked");
        callback.onSellWithExtraPaymentBtnClicked();
    }

    private void onDecrementTripBtnClicked() {
        Logger.info(TAG, "onDecrementTripBtnClicked");
        callback.onDecrementTripBtnClicked();
    }

    public void navigateToDecrementTripActivity(DecrementTripParams decrementTripParams) {
        Navigator.navigateToDecrementTripActivity(getActivity(), this, decrementTripParams, RC_DECREMENT_TRIP);
    }

    interface Callback {
        void onZoomDialogShown();

        void onZoomDialogHidden();

        void onDecrementTripResultReturned(@Nullable DecrementTripResult decrementTripResult);

        void onDecrementTripBtnClicked();

        void onSellWithExtraPaymentBtnClicked();

        void onPdNotValidBtnClicked();
    }

}
