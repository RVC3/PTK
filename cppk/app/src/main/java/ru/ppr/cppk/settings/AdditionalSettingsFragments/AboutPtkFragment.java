package ru.ppr.cppk.settings.AdditionalSettingsFragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.selectionActivity.BindingStationSelectionActivity;
import ru.ppr.cppk.ui.activity.selectionActivity.ProductionSectionSelectionActivity;
import ru.ppr.nsi.entity.ProductionSection;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.repository.ProductionSectionRepository;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.security.entity.PermissionDvc;

/**
 * Фрагмент настроек "Об устройстве".
 */
public class AboutPtkFragment extends FragmentParent {

    /**
     * Код запуска активити выбора участка работы ПТК
     */
    public static final int REQUEST_CODE_SET_PRODUCTION_SECTION = 3245234;
    /**
     * Код запуска активити выбора станции привязки ПТК
     */
    public static final int REQUEST_CODE_SET_BINDING_STATION = 3245235;

    private ViewGroup ptkAreaLayout;
    private TextView ptkAreaTextView;
    private View ptkAreaMoreImage;

    private ViewGroup ptkMobileCashLayout;
    private ImageView ptkMobileCashImageView;
    private View ptkMobileCashMoreImage;

    private ViewGroup ptkBindingStationLayout;
    private TextView ptkBindingStationTextView;
    private View ptkBindingStationMoreImage;

    private Globals globals;
    private Holder<PrivateSettings> privateSettingsHolder;
    private NsiVersionManager nsiVersionManager;
    private StationRepository stationRepository;
    private ProductionSectionRepository productionSectionRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globals = Globals.getInstance();
        privateSettingsHolder = globals.getPrivateSettingsHolder();
        nsiVersionManager = Di.INSTANCE.nsiVersionManager();
        stationRepository = Dagger.appComponent().stationRepository();
        productionSectionRepository = Dagger.appComponent().productionSectionRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.additional_setting_about_ptk_fragment, container, false);

        final TextView ptkNumberTextView = (TextView) view.findViewById(R.id.additional_setting_ptk_number_value);
        ptkNumberTextView.setText(String.valueOf(privateSettingsHolder.get().getTerminalNumber()));

        ptkAreaTextView = (TextView) view.findViewById(R.id.additional_setting_ptk_area_value);
        ptkBindingStationTextView = (TextView) view.findViewById(R.id.additional_setting_ptk_binding_station_value);
        ptkMobileCashImageView = (ImageView) view.findViewById(R.id.additional_settings_mobile_cash_register_flag);

        ptkAreaMoreImage = view.findViewById(R.id.additional_settings_ptk_area_more);
        ptkBindingStationMoreImage = view.findViewById(R.id.additional_settings_ptk_binding_station_more);
        ptkMobileCashMoreImage = view.findViewById(R.id.additional_settings_mobile_cash_register_more);

        ptkAreaLayout = (ViewGroup) view.findViewById(R.id.additional_settings_ptk_area_layout);
        ptkAreaLayout.setOnClickListener(v -> changeProductionSection());

        ptkBindingStationLayout = (ViewGroup) view.findViewById(R.id.additional_settings_ptk_binding_station_layout);
        ptkBindingStationLayout.setOnClickListener(v -> changeBindingStation());

        ptkMobileCashLayout = (ViewGroup) view.findViewById(R.id.additional_settings_mobile_cash_layout);
        ptkMobileCashLayout.setOnClickListener(v -> changeMobileCashRegister());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        setProductionSection();
        setBindingStation();
        setMobileCashRegister();
    }

    /**
     * Метод для получения участка обслуживания ПТК.
     */
    @Nullable
    private ProductionSection getCurrentProductionSection() {
        final int pSectionId = privateSettingsHolder.get().getProductionSectionId();
        return productionSectionRepository.load((long) pSectionId, nsiVersionManager.getCurrentNsiVersionId());
    }

    /**
     * Метод для получения станции привязки ПТК
     */
    @Nullable
    private Station getCurrentBindingStation() {
        return stationRepository.load((long) privateSettingsHolder.get().getSaleStationCode(), nsiVersionManager.getCurrentNsiVersionId());
    }

    /**
     * Метод для обновления участка обслуживания ПТК.
     */
    private void setProductionSection() {
        final ProductionSection productionSection = getCurrentProductionSection();
        final boolean isCanChangeWorkingPlace = hasPermission(PermissionDvc.ChangeWorkingPlace);

        ptkAreaLayout.setEnabled(isCanChangeWorkingPlace);
        ptkAreaLayout.setBackgroundResource(isCanChangeWorkingPlace ? R.color.white : R.color.gray_inactive);
        ptkAreaTextView.setText(productionSection != null ? productionSection.getName() : getResources().getString(R.string.not_specified));
        ptkAreaMoreImage.setVisibility((isCanChangeWorkingPlace) ? View.VISIBLE : View.GONE); //скроем стрелочку если настройка недоступна
    }

    /**
     * Метод для установления станции привязки ПТК.
     */
    private void setBindingStation() {
        final Station station = getCurrentBindingStation();

        if (station != null) {
            ptkBindingStationTextView.setText(station.getName());
        } else {
            ptkBindingStationTextView.setText(getResources().getString(R.string.not_specified));
        }

        final boolean isCanChangeBindingStation = hasPermission(PermissionDvc.ChangeBindingStation) && getCurrentProductionSection() != null;
        ptkBindingStationLayout.setEnabled(isCanChangeBindingStation);
        ptkBindingStationLayout.setBackgroundResource(isCanChangeBindingStation ? R.color.white : R.color.gray_inactive);
        ptkBindingStationMoreImage.setVisibility((isCanChangeBindingStation) ? View.VISIBLE : View.GONE);
    }

    /**
     * Метод для изменения участка убслуживания.
     */
    private void changeProductionSection() {
        if (hasPermission(PermissionDvc.ChangeWorkingPlace)) {
            Navigator.navigateToProductionSectionSelectionActivity(getActivity(), this, REQUEST_CODE_SET_PRODUCTION_SECTION);
        } else {
            showErrorMessage();
        }
    }

    /**
     * Метод для изменения станции привязки
     */
    private void changeBindingStation() {
        if (hasPermission(PermissionDvc.ChangeBindingStation)) {
            Navigator.navigateToBindingStationSelectionActivity(getActivity(), this, REQUEST_CODE_SET_BINDING_STATION);
        } else {
            showErrorMessage();
        }
    }

    /**
     * Метод для обновления мобильной кассы.
     */
    private void setMobileCashRegister() {
        final boolean isCanChangeMobileCashierMode = hasPermission(PermissionDvc.ActivateMobileCashierMode);

        ptkMobileCashLayout.setEnabled(isCanChangeMobileCashierMode);
        ptkMobileCashLayout.setBackgroundResource(isCanChangeMobileCashierMode ? R.color.white : R.color.gray_inactive);
        ptkMobileCashImageView.setVisibility(privateSettingsHolder.get().isMobileCashRegister() ? View.VISIBLE : View.GONE);
        ptkMobileCashMoreImage.setVisibility((isCanChangeMobileCashierMode) ? View.VISIBLE : View.GONE); //скроем стрелочку если настройка недоступна
    }

    /**
     * Метод для изменения режима работы «Мобильной кассы».
     */
    private void changeMobileCashRegister() {
        if (hasPermission(PermissionDvc.ActivateMobileCashierMode)) {
            Navigator.navigateToMobileCashSettingsActivity(this.getActivity());
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getExtras() != null && resultCode == Activity.RESULT_OK) {
            if (REQUEST_CODE_SET_PRODUCTION_SECTION == requestCode) {
                final PrivateSettings settings = new PrivateSettings(privateSettingsHolder.get());
                settings.setProductionSectionId(data.getExtras().getInt(ProductionSectionSelectionActivity.EXTRA_RESULT_CODE, PrivateSettings.Default.PRODUCTION_SECTION_CODE));
                settings.setCurrentStationCode(PrivateSettings.Default.WORK_STATION_CODE);
                settings.setMobileCashRegister(PrivateSettings.Default.IS_MOBILE_CASH_REGISTER);
                settings.setIsOutputMode(PrivateSettings.Default.IS_OUTPUT_MODE);
                settings.setSaleStationCode(PrivateSettings.Default.SALE_STATION_CODE);

                Dagger.appComponent().privateSettingsRepository().savePrivateSettings(settings);
                privateSettingsHolder.set(settings);

                Di.INSTANCE.getDeviceSessionInfo().setCurrentStationDevice(StationDevice.getThisDevice());

            } else if (REQUEST_CODE_SET_BINDING_STATION == requestCode) {
                final int code = data.getIntExtra(BindingStationSelectionActivity.EXTRA_RESULT_CODE, -1);
                if (code != -1) {
                    final PrivateSettings settings = new PrivateSettings(privateSettingsHolder.get());
                    settings.setSaleStationCode(code);
                    Dagger.appComponent().privateSettingsRepository().savePrivateSettings(settings);
                    privateSettingsHolder.set(settings);
                    Di.INSTANCE.getDeviceSessionInfo().setCurrentStationDevice(StationDevice.getThisDevice());
                }
            }
        }
    }

    /**
     * Метод для отображении информаци об ошибке.
     */
    private void showErrorMessage() {
        ((SystemBarActivity) getActivity()).makeErrorAccessToast();
    }

}
