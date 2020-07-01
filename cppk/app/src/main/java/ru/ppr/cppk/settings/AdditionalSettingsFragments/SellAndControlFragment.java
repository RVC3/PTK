package ru.ppr.cppk.settings.AdditionalSettingsFragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.local.repository.PrivateSettingsRepository;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TrainCategoryPrefix;
import ru.ppr.nsi.model.TransferStationRoute;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.security.entity.PermissionDvc;

/**
 * Фрагмент настроек "Продажа и контроль".
 */
public class SellAndControlFragment extends FragmentParent {

    private static final String TAG = Logger.makeLogTag(SellAndControlFragment.class);

    public static final String FROM_OPEN_SHIFT_SETTINGS = "fromOpenShiftSettings";
    public static final String FROM_SPECIAL_ACTIVITY = "fromSpecialActivity";

    private static final String dateFormat = "dd MMM yyyy";
    private static final String timeFormat = "kk : mm";

    //region UI Views

    /**
     * Разделитель между блоками "Код дня" и "Доступность продажи трансфера"
     */
    private View dayCodeAndTransferSellingPossibilitySeparator;
    /**
     * Разделитель между блоками "Доступность продажи трансфера" и "Тип контроля"
     */
    private View transferSellingPossibilityAndControlTypeSeparator;
    /**
     * Разделитель между блоками "Тип контроля" и "Тип поезда контроля/маршрут трансфера"
     */
    private View controlTypeAndTrainTypeSeparator;
    /**
     * Разделитель после блока "Тип поезда контроля/маршрут трансфера"
     */
    private View transferRouteBottomSeparator;

    //region Код дня
    private ViewGroup changeDayCodeLayout;
    private TextView dayCodeTextView;
    private View dayCodeArrowView;
    //endregion

    //region Доступность продажи трансфера
    private ViewGroup transferSellingPossibilityLayout;
    private ImageView transferSellingPossibilityImageView;
    //endregion

    //region Режим контроля
    private View controlModeCategoryLabel;
    private RadioGroup controlModeGroup;
    private RadioButton controlModeTrain, controlModeBus;
    //endregion

    //region Категория поезда
    private View trainCategoryLabel;
    private RadioGroup trainGroup;
    private RadioButton train6000, train7000;
    //endregion

    //region блок настройки трансфера
    private ViewGroup transferLayout;

    //region Маршрут трансфера
    private ViewGroup changeTransferRouteLayout;
    private TextView transferRouteDepStationValue;
    private TextView transferRouteDestStationValue;
    private View transferRouteArrow;
    //endregion

    //region Направление движения трансфера
    private ViewGroup changeTransferRouteDirectionLayout;
    private TextView transferRouteDirectionValue;
    private View transferRouteDirectionArrow;
    //endregion

    //region Дата и время отправления трансфера
    private ViewGroup additionalSettingsTransferDepartureDateLayout;
    private TextView additionalSettingsTransferDepartureDateValue;
    private View additionalSettingsTransferDepartureDateArrow;

    private ViewGroup additionalSettingsTransferDepartureTimeLayout;
    private TextView additionalSettingsTransferDepartureTimeValue;
    private View additionalSettingsTransferDepartureTimeArrow;
    //endregion

    //endregion

    //endregion

    private NsiVersionManager nsiVersionManager;
    private StationRepository stationRepository;
    private Holder<PrivateSettings> privateSettingsHolder;
    private PrivateSettingsRepository privateSettingsRepository;
    private ShiftManager shiftManager;
    private Globals app;

    private long transferDepartureStationCode = 0;
    private long transferDestinationStationCode = 0;

    private Calendar transferDepartureDateTime = Calendar.getInstance();

    private TransferStationRoute transferRoute = null;

    public static SellAndControlFragment newInstance(boolean fromOpenShiftSettings, boolean fromSpecialActivity) {
        SellAndControlFragment sellAndControlFragment = new SellAndControlFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(SellAndControlFragment.FROM_OPEN_SHIFT_SETTINGS, fromOpenShiftSettings);
        bundle.putBoolean(SellAndControlFragment.FROM_SPECIAL_ACTIVITY, fromSpecialActivity);
        sellAndControlFragment.setArguments(bundle);
        return sellAndControlFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nsiVersionManager = Dagger.appComponent().nsiVersionManager();
        stationRepository = Dagger.appComponent().stationRepository();
        privateSettingsHolder = Dagger.appComponent().privateSettingsHolder();
        privateSettingsRepository = Dagger.appComponent().privateSettingsRepository();
        shiftManager = Dagger.appComponent().shiftManager();
        app = Dagger.appComponent().app();
        transferDepartureDateTime.set(Calendar.SECOND, 0);
        transferDepartureDateTime.set(Calendar.MILLISECOND, 0);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final boolean fromOpenShiftSettings = (args != null) && args.getBoolean(FROM_OPEN_SHIFT_SETTINGS);
        final boolean fromSpecialActivity = (args != null) && args.getBoolean(FROM_SPECIAL_ACTIVITY);

        final View view = inflater.inflate(R.layout.additional_setting_sell_and_control_fragment, container, false);

        findViews(view);

        setListeners();

        configVisibility(fromOpenShiftSettings, fromSpecialActivity);

        return view;
    }

    private void findViews(final View view) {
        dayCodeAndTransferSellingPossibilitySeparator = view.findViewById(R.id.additionalSettingsDayCodeAndTransferSellingPossibilitySeparator);
        transferSellingPossibilityAndControlTypeSeparator = view.findViewById(R.id.additionalSettingsTransferSellingPossibilityAndControlTypeSeparator);
        controlTypeAndTrainTypeSeparator = view.findViewById(R.id.additionalSettingsControlTypeAndTrainTypeSeparator);
        transferRouteBottomSeparator = view.findViewById(R.id.additionalSettingsTransferRouteBottomSeparator);

        changeDayCodeLayout = (LinearLayout) view.findViewById(R.id.layoutChangeDayCode);
        dayCodeTextView = (TextView) view.findViewById(R.id.additional_settings_day_code);
        dayCodeArrowView = view.findViewById(R.id.additional_settings_day_code_arrow);

        transferSellingPossibilityLayout = (RelativeLayout) view.findViewById(R.id.additionalSettingsTransferSellingPossibilityLayout);
        transferSellingPossibilityImageView = (ImageView) view.findViewById(R.id.additionalSettingsTransferSellingPossibilityCheckBox);

        controlModeGroup = (RadioGroup) view.findViewById(R.id.additional_settings_control_type_group);
        controlModeCategoryLabel = view.findViewById(R.id.additional_setting_control_type_label);
        controlModeBus = (RadioButton) view.findViewById(R.id.additional_setting_control_type_mode_bus);
        controlModeTrain = (RadioButton) view.findViewById(R.id.additional_setting_control_type_mode_train);

        trainGroup = (RadioGroup) view.findViewById(R.id.additional_settings_train_group);
        trainCategoryLabel = view.findViewById(R.id.additional_setting_category_label);
        train6000 = (RadioButton) view.findViewById(R.id.additional_setting_train_6000);
        train7000 = (RadioButton) view.findViewById(R.id.additional_setting_train_7000);

        transferLayout = (LinearLayout) view.findViewById(R.id.additionalSettingsTransferLayout);

        changeTransferRouteLayout = (LinearLayout) view.findViewById(R.id.additionalSettingsChangeTransferRouteLayout);
        transferRouteDepStationValue = (TextView) view.findViewById(R.id.additionalSettingsTransferRouteDepStationValue);
        transferRouteDestStationValue = (TextView) view.findViewById(R.id.additionalSettingsTransferRouteDestStationValue);
        transferRouteArrow = view.findViewById(R.id.additionalSettingsTransferRouteArrow);

        changeTransferRouteDirectionLayout = (LinearLayout) view.findViewById(R.id.additionalSettingsChangeTransferRouteDirectionLayout);
        transferRouteDirectionValue = (TextView) view.findViewById(R.id.additionalSettingsTransferRouteDirectionValue);
        transferRouteDirectionArrow = view.findViewById(R.id.additionalSettingsTransferRouteDirectionArrow);

        additionalSettingsTransferDepartureDateLayout = (LinearLayout) view.findViewById(R.id.additionalSettingsTransferDepartureDateLayout);
        additionalSettingsTransferDepartureDateValue = (TextView) view.findViewById(R.id.additionalSettingsTransferDepartureDateValue);
        additionalSettingsTransferDepartureDateArrow = view.findViewById(R.id.additionalSettingsTransferDepartureDateArrow);

        additionalSettingsTransferDepartureTimeLayout = (LinearLayout) view.findViewById(R.id.additionalSettingsTransferDepartureTimeLayout);
        additionalSettingsTransferDepartureTimeValue = (TextView) view.findViewById(R.id.additionalSettingsTransferDepartureTimeValue);
        additionalSettingsTransferDepartureTimeArrow = view.findViewById(R.id.additionalSettingsTransferDepartureTimeArrow);
    }

    private void configVisibility(boolean fromOpenShiftSettings, boolean fromSpecialActivity) {
        if (fromOpenShiftSettings || fromSpecialActivity) {
            changeDayCodeLayout.setVisibility(View.GONE);
            transferSellingPossibilityLayout.setVisibility(View.GONE);
            controlModeCategoryLabel.setVisibility(View.GONE);
            controlModeGroup.setVisibility(View.GONE);
            dayCodeAndTransferSellingPossibilitySeparator.setVisibility(View.GONE);
            transferSellingPossibilityAndControlTypeSeparator.setVisibility(View.GONE);
            controlTypeAndTrainTypeSeparator.setVisibility(View.GONE);
        }

        if (fromSpecialActivity) {
            trainCategoryLabel.setVisibility(View.GONE);
            transferRouteBottomSeparator.setVisibility(View.VISIBLE);
        }

        if (fromOpenShiftSettings) {
            if (privateSettingsHolder.get().isMobileCashRegister()) {
                trainGroup.setVisibility(View.GONE);
            }
        }
    }

    private void setListeners() {
        changeDayCodeLayout.setOnClickListener(v -> changeDayCode());
        transferSellingPossibilityLayout.setOnClickListener(v -> changeTransferSellingPossibility());
        train6000.setOnClickListener(v -> changeTrainCategory(TrainCategoryPrefix.PASSENGER));
        train7000.setOnClickListener(v -> changeTrainCategory(TrainCategoryPrefix.EXPRESS));
        controlModeBus.setOnClickListener(v -> changeControlMode(true));
        controlModeTrain.setOnClickListener(v -> changeControlMode(false));
        changeTransferRouteLayout.setOnClickListener(v -> changeTransferRoute());
        changeTransferRouteDirectionLayout.setOnClickListener(v -> changeTransferRouteDirection());
        additionalSettingsTransferDepartureDateLayout.setOnClickListener(v -> changeTransferDepartureDate());
        additionalSettingsTransferDepartureTimeLayout.setOnClickListener(v -> changeTransferDepartureTime());
    }

    /**
     * Метод для обновления настройки "Доступность продажи трансфера"
     */
    private void changeTransferSellingPossibility() {
        PrivateSettings privateSettings = privateSettingsHolder.get();
        privateSettings.setTransferSaleEnabled(!privateSettings.isTransferSaleEnabled());
        privateSettingsRepository.savePrivateSettings(privateSettings);
        updateTransferSellingPossibilityValue();
    }

    private void changeTransferRouteDirection() {
        if (hasPermission(PermissionDvc.ChangeTransferRoute)) {
            long[] codes = privateSettingsHolder.get().getTransferRouteStationsCodes();
            long[] newCodes = {codes[1], codes[0]};
            privateSettingsHolder.get().setTransferRouteStationsCodes(newCodes);
            privateSettingsRepository.savePrivateSettings(privateSettingsHolder.get());

            // http://agile.srvdev.ru/browse/CPPKPP-43972
            // Изменять время отправления при смене направления трансфера
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date transferDepartureDateTime = calendar.getTime();
            SharedPreferencesUtils.setTransferDepartureDateTime(Dagger.appComponent().app(), transferDepartureDateTime);

            setTransferRoute();
            setTransferDepartureDateTime();
        }
    }

    private void changeTransferRoute() {
        // Можем менять станции маршрута трансфера только при закрытой смене
        if (shiftManager.isShiftClosed()) {
            if (hasPermission(PermissionDvc.ChangeTransferRoute)) {
                Navigator.navigateToSelectTransferStationsActivity(getActivity());
            } else
                showErrorMessage();
        }
    }

    private void changeTransferDepartureDate() {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view1, year1, monthOfYear, dayOfMonth) -> {
            if (view1.isShown()) {
                Logger.trace(TAG, "Ручное исправление даты отправления трансфера");
                transferDepartureDateTime.set(Calendar.YEAR, year1);
                transferDepartureDateTime.set(Calendar.MONTH, monthOfYear);
                transferDepartureDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setTransferDepartureDate();
                SharedPreferencesUtils.setTransferDepartureDateTime(app, transferDepartureDateTime.getTime());
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    private void changeTransferDepartureTime() {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), (view1, hourOfDay1, minute1) -> {
            if (view1.isShown()) {
                Logger.trace(TAG, "Ручное исправление времени отправления трансфера");
                transferDepartureDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay1);
                transferDepartureDateTime.set(Calendar.MINUTE, minute1);
                setTransferDepartureTime();
                SharedPreferencesUtils.setTransferDepartureDateTime(app, transferDepartureDateTime.getTime());
            }
        }, hourOfDay, minute, true);
        timePickerDialog.show();
    }

    private void setTransferDepartureDateTime() {
        Date savedTransferDepartureDateTime = SharedPreferencesUtils.getTransferDepartureDateTime(app);
        if (savedTransferDepartureDateTime != null) {
            transferDepartureDateTime.setTime(savedTransferDepartureDateTime);
        } else {
            SharedPreferencesUtils.setTransferDepartureDateTime(app, transferDepartureDateTime.getTime());
        }
        setTransferDepartureDate();
        setTransferDepartureTime();
    }

    private void setTransferDepartureDate() {
        CharSequence date = android.text.format.DateFormat.format(dateFormat, transferDepartureDateTime.getTime());
        additionalSettingsTransferDepartureDateValue.setText(date);
    }

    private void setTransferDepartureTime() {
        CharSequence time = android.text.format.DateFormat.format(timeFormat, transferDepartureDateTime.getTime());
        additionalSettingsTransferDepartureTimeValue.setText(time);
    }

    @Override
    public void onResume() {
        super.onResume();
        setDayCode();
        setTrainCategory();
        setControlMode();
        setTransferRoute();
        setTransferDepartureDateTime();
        updateTransferSellingPossibilityValue();
    }

    /**
     * Метод для обновления кода дня.
     */
    private void setDayCode() {
        final boolean isCanSetDayCode = hasPermission(PermissionDvc.ChangeDayCode);

        changeDayCodeLayout.setEnabled(isCanSetDayCode);
        changeDayCodeLayout.setBackgroundResource(isCanSetDayCode ? R.color.white : R.color.gray_inactive);
        dayCodeTextView.setText(String.format(Locale.getDefault(), "%04d", privateSettingsHolder.get().getDayCode()));
        dayCodeArrowView.setVisibility(isCanSetDayCode ? View.VISIBLE : View.GONE);
    }

    /**
     * Обновление галки настройки "Доступность продажи трансфера"
     */
    private void updateTransferSellingPossibilityValue() {
        final boolean isCanChange = hasPermission(PermissionDvc.ChangeTransferSellingPossibility);
        transferSellingPossibilityLayout.setEnabled(isCanChange);
        transferSellingPossibilityLayout.setBackgroundResource(isCanChange ? R.color.white : R.color.gray_inactive);
        transferSellingPossibilityImageView.setVisibility(privateSettingsHolder.get().isTransferSaleEnabled() ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Метод для обновления маршрута трансфера
     */
    private void setTransferRoute() {
        final boolean isCanChangeTransferRoute = hasPermission(PermissionDvc.ChangeTransferRoute);
        long[] stationsCodes = privateSettingsHolder.get().getTransferRouteStationsCodes();
        long fromStationCode = stationsCodes[0];
        long toStationCode =  stationsCodes[1];

        if (transferDepartureStationCode != fromStationCode || transferDestinationStationCode != toStationCode) {
            this.transferRoute = findTransferRoute(fromStationCode, toStationCode);

            Station fromStation = fromStationCode > 0 ? stationRepository.load(fromStationCode, nsiVersionManager.getCurrentNsiVersionId()) : null;
            Station toStation = toStationCode > 0 ? stationRepository.load(toStationCode, nsiVersionManager.getCurrentNsiVersionId()) : null;

            String routeDepStationText = "";
            String routeDestStationText = "";

            if (fromStation != null && toStation != null && transferRoute != null) {
                if (transferRoute.getDepStationCode() == fromStationCode && transferRoute.getDestStationCode() == toStationCode) {
                    routeDepStationText = fromStation.getName();
                    routeDestStationText = toStation.getName();
                } else if (transferRoute.getDepStationCode() == toStationCode && transferRoute.getDestStationCode() == fromStationCode) {
                    routeDepStationText = toStation.getName();
                    routeDestStationText = fromStation.getName();
                }
            }

            changeTransferRouteLayout.setEnabled(isCanChangeTransferRoute);
            changeTransferRouteLayout.setBackgroundResource(isCanChangeTransferRoute ? R.color.white : R.color.gray_inactive);
            transferRouteDepStationValue.setText(routeDepStationText);
            transferRouteDestStationValue.setText(routeDestStationText);

            // Показываем стрелку на кнопке Маршрут трансфера только при закрытой смене
            boolean isShiftClosed = shiftManager.isShiftClosed();
            transferRouteArrow.setVisibility(isCanChangeTransferRoute && isShiftClosed ? View.VISIBLE : View.GONE);

            changeTransferRouteDirectionLayout.setEnabled(isCanChangeTransferRoute);
            changeTransferRouteDirectionLayout.setBackgroundResource(isCanChangeTransferRoute ? R.color.white : R.color.gray_inactive);
            transferRouteDirectionValue.setText((fromStation != null && toStation != null) ? getActivity().getString(R.string.additional_settings_transfer_route_direction_value, fromStation.getName()) : "");
            transferRouteDirectionArrow.setVisibility(isCanChangeTransferRoute ? View.VISIBLE : View.GONE);

            transferDepartureStationCode = fromStationCode;
            transferDestinationStationCode = toStationCode;
        }
    }

    private TransferStationRoute findTransferRoute(long fromStationCode, long toStationCode) {
        if (fromStationCode <= 0 || toStationCode <= 0) return null;

        //сначала посмотрим в кеше
        if (transferRoute != null && transferRoute.getDepStationCode() == fromStationCode && transferRoute.getDestStationCode() == toStationCode) {
            return transferRoute;
        } else if (transferRoute != null && transferRoute.getDepStationCode() == toStationCode && transferRoute.getDestStationCode() == fromStationCode) {
            return transferRoute;
        }

        return stationRepository.loadTransferRouteForStations(fromStationCode, toStationCode, nsiVersionManager.getCurrentNsiVersionId());
    }

    /**
     * Метод для установления кода дня.
     */
    private void changeDayCode() {
        if (hasPermission(PermissionDvc.ChangeDayCode)) {
            Navigator.navigateToEnterDayCodeActivity(getActivity());
        } else
            showErrorMessage();
    }

    /**
     * Метод для изменения режима контроля.
     *
     * @param isBusMode
     */
    private void changeControlMode(boolean isBusMode) {
        if (hasPermission(PermissionDvc.ChangeControlMode)) {
            PrivateSettings privateSettings = privateSettingsHolder.get();

            privateSettings.setTransferControlMode(isBusMode);

            if (privateSettings.isTransferControlMode()) {
                // http://agile.srvdev.ru/browse/CPPKPP-41171
                // Сделано по аналогии с включением режима мобильной кассы
                privateSettings.setTrainCategoryPrefix(TrainCategoryPrefix.PASSENGER);
            }

            privateSettingsRepository.savePrivateSettings(privateSettings);
            setControlMode();

            boolean isBus = privateSettingsHolder.get().isTransferControlMode();
            if (!isBus) {
                SharedPreferencesUtils.setTransferDepartureDateTime(app, null);
            } else {
                setTransferDepartureDateTime();
            }
            setTrainCategory();
        } else {
            showErrorMessage();
        }
    }

    /**
     * Метод для изменения категории поезда.
     *
     * @param prefix префикс категории поезда.
     * @see TrainCategoryPrefix
     */
    private void changeTrainCategory(@NonNull final TrainCategoryPrefix prefix) {
        if (hasPermission(PermissionDvc.ChangeTrainCategoryCode)) {
            privateSettingsHolder.get().setTrainCategoryPrefix(prefix);
            privateSettingsRepository.savePrivateSettings(privateSettingsHolder.get());
        } else {
            showErrorMessage();
        }
    }

    /**
     * Метод для обновления категории поезда.
     */
    private void setControlMode() {
        final boolean isEnabled = hasPermission(PermissionDvc.ChangeControlMode);
        boolean isBus = privateSettingsHolder.get().isTransferControlMode();

        controlModeBus.setEnabled(isEnabled);
        controlModeBus.setChecked(isBus);
        controlModeBus.setBackgroundResource(isEnabled ? R.color.white : R.color.gray_inactive);

        controlModeTrain.setEnabled(isEnabled);
        controlModeTrain.setChecked(!isBus);
        controlModeTrain.setBackgroundResource(isEnabled ? R.color.white : R.color.gray_inactive);

        trainGroup.setVisibility(isBus ? View.GONE : View.VISIBLE);
        transferLayout.setVisibility(isBus ? View.VISIBLE : View.GONE);
    }

    /**
     * Метод для обновления категории поезда.
     */
    private void setTrainCategory() {
        final TrainCategoryPrefix prefix = privateSettingsHolder.get().getTrainCategoryPrefix();
        final boolean isExpress = (prefix == TrainCategoryPrefix.EXPRESS);
        final boolean isEnabled = hasPermission(PermissionDvc.ChangeTrainCategoryCode);

        if (privateSettingsHolder.get().isMobileCashRegister()) {
            train7000.setEnabled(false);
        } else {
            train7000.setEnabled(isEnabled);
        }

        train6000.setEnabled(isEnabled);
        train6000.setChecked(!isExpress);
        train6000.setBackgroundResource(train6000.isEnabled() ? R.color.white : R.color.gray_inactive);

        train7000.setChecked(isExpress);
        train7000.setBackgroundResource(train7000.isEnabled() ? R.color.white : R.color.gray_inactive);
    }

    /**
     * Метод для отображении информации об ошибке.
     */
    private void showErrorMessage() {
        ((SystemBarActivity) getActivity()).makeErrorAccessToast();
    }

}
