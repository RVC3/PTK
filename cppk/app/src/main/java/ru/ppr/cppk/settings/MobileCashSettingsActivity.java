package ru.ppr.cppk.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TrainCategoryPrefix;

/**
 * Класс экрана настроек «Мобильной кассы».
 */
public class MobileCashSettingsActivity extends SystemBarActivity {

    /**
     * Код запуска активити выбора станции работы ПТК
     */
    public static final int REQUEST_CODE_SET_WORK_STATION = 1;

    private ImageView adtSettingIsActivatedCheckBox;
    private ImageView adtSettingOutputCheckbox;
    private TextView workplaceValue;

    private boolean isStationSelected;
    private PrivateSettings privateSettings;

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, MobileCashSettingsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_mobile_cash);

        adtSettingIsActivatedCheckBox = (ImageView) findViewById(R.id.adtSettingIsActivatedCheckBox);
        adtSettingOutputCheckbox = (ImageView) findViewById(R.id.adtSettingOutputCheckbox);
        workplaceValue = (TextView) findViewById(R.id.additional_settings_work_place_value);

        privateSettings = di().getPrivateSettings().get();

        final int version = Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId();
        final long stationCode = privateSettings.getCurrentStationCode();
        final Station station = Dagger.appComponent().stationRepository().load(stationCode, version);

        if (station != null) {
            workplaceValue.setText(station.getName());
            isStationSelected = true;
        }

        setMobileCashRegister(privateSettings.isMobileCashRegister());
        setOutputMode(privateSettings.isOutputMode());
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.additional_setting_is_activated_layout:
                changeMobileCashRegister();
                break;

            case R.id.additional_setting_output_layout:
                changeOutputMode();
                break;

            case R.id.additional_setting_work_station_layout:
                goToChooseWorkStation();
                break;

            default:
                break;
        }

    }

    /**
     * Метод для отображения выбранной станции
     */
    private void setWorkStation(Station station) {
        workplaceValue = (TextView) findViewById(R.id.additional_settings_work_place_value);
        workplaceValue.setText(station == null ? "" : station.getShortName());
    }

    /**
     * Метод для выбора станции работы ПТК.
     */
    private void goToChooseWorkStation() {
        if (privateSettings.isMobileCashRegister()) {
            Navigator.navigateToStationSelectionActivity(this, null, REQUEST_CODE_SET_WORK_STATION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            final int code = data.getIntExtra("code", -1);
            final String name = data.getStringExtra("name");

            if (code != -1) {
                isStationSelected = true;
                privateSettings.setCurrentStationCode(code);

                Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);

                setWorkStation(Dagger.appComponent().stationRepository().load((long) code, Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId()));
            }
        }
    }

    /**
     * Метод для обновления режима работы «Мобильной кассы».
     */
    private void changeMobileCashRegister() {
        privateSettings.setMobileCashRegister(!privateSettings.isMobileCashRegister());
        privateSettings.setIsOutputMode(false);

        if (privateSettings.isMobileCashRegister()) {
            privateSettings.setTrainCategoryPrefix(TrainCategoryPrefix.PASSENGER);
        } else {
            privateSettings.setCurrentStationCode(-1);
            isStationSelected = false;
        }

        Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);

        setMobileCashRegister(privateSettings.isMobileCashRegister());
        setOutputMode(privateSettings.isOutputMode());
        setWorkStation(Dagger.appComponent().stationRepository().load((long) privateSettings.getCurrentStationCode(), Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId()));
    }

    /**
     * Метод для изменения режима работы «Мобильной кассы» на выход.
     *
     * @see PrivateSettings#isOutputMode()
     */
    private void changeOutputMode() {
        if (privateSettings.isMobileCashRegister()) {
            privateSettings.setIsOutputMode(!privateSettings.isOutputMode());

            Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);

            setOutputMode(privateSettings.isOutputMode());
        } else {
            Globals.getInstance().getToaster().showToast(R.string.is_not_activated_msg);
        }
    }

    /**
     * Метод для обновления view.
     *
     * @param visible значение для отображении view.
     */
    private void setOutputMode(final boolean visible) {
        if (visible) {
            adtSettingOutputCheckbox.setVisibility(View.VISIBLE);
        } else {
            adtSettingOutputCheckbox.setVisibility(View.GONE);
        }
    }

    /**
     * Метод для обновления view.
     *
     * @param visible значение для отображении view.
     */
    private void setMobileCashRegister(final boolean visible) {
        if (visible) {
            adtSettingIsActivatedCheckBox.setVisibility(View.VISIBLE);
        } else {
            adtSettingIsActivatedCheckBox.setVisibility(View.GONE);
        }
    }

    /**
     * Показывает диалог о необходимости выбрать станцию работы ПТК
     */
    private void showErrorDialog() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(
                null,
                getString(R.string.not_choose_work_station),
                getString(R.string.dialog_close),
                null,
                LinearLayout.VERTICAL,
                -1
        );
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (privateSettings.isMobileCashRegister()) { //если включен режим мобильной кассы
                if (isStationSelected) { // проверим, выбрана ли станция работы птк
                    finish();
                } else {
                    showErrorDialog(); //если не выбрана, то показываем сообщение, и остаемся на этом же окне
                }
            } else {
                finish();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}