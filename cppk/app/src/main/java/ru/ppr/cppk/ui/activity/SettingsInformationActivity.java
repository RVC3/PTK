package ru.ppr.cppk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.TextView;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.systembar.SystemBarActivity;

/**
 * Created by григорий on 12.10.2016.
 * Окно информации о моделях оборудования и версиях прошивок.
 */
public class SettingsInformationActivity extends SystemBarActivity {

    private TextView bscReaderModel;
    private TextView bscReaderFirmware;
    private TextView barcodeReaderModel;
    private TextView barcodeReaderFirmware;
    private TextView phoneModel;
    private TextView phoneFirmware;

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, SettingsInformationActivity.class);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_information);

        bscReaderModel = (TextView) findViewById(R.id.di_bscReaderModel);
        bscReaderFirmware = (TextView) findViewById(R.id.di_bscReaderFirmware);
        barcodeReaderModel = (TextView) findViewById(R.id.di_barcodeReaderModel);
        barcodeReaderFirmware = (TextView) findViewById(R.id.di_barcodeReaderFirmware);
        phoneModel = (TextView) findViewById(R.id.di_phoneReaderModel);
        phoneFirmware = (TextView) findViewById(R.id.di_phoneReaderFirmware);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAllInformation();
    }

    private void updateAllInformation() {
        updateBscReaderModel();
        updateBscReaderFirmware();
        updateBarcodeReaderModel();
        updateBarcodeReaderFirmware();
        setValue(phoneModel, getPhoneModel());
        setValue(phoneFirmware, getPhoneFirmware());
    }

    private void updateBarcodeReaderFirmware() {
        SchedulersCPPK.rfidExecutorService().execute(() -> {
            StringBuilder version = new StringBuilder();
            Dagger.appComponent().barcodeReader().open();
            Dagger.appComponent().barcodeReader().getFirmwareVersion(version);
            runOnUiThread(() -> {
                setValue(barcodeReaderFirmware, version.toString());
            });
        });

    }

    @NonNull
    public static String getPhoneFirmware() {
        return Build.DISPLAY;
    }

    private void setValue(TextView tView, String value) {
        if (!TextUtils.isEmpty(value)) tView.setText(value);
        else tView.setText(R.string.devicesInformation_NoData);
    }

    public static String getPhoneModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    public void updateBscReaderFirmware() {
        SchedulersCPPK.rfidExecutorService().execute(() -> {
            String[] version = new String[1];
            Dagger.appComponent().rfid().getFWVersion(version);
            runOnUiThread(() -> {
                setValue(bscReaderFirmware, version[0]);
            });
        });
    }

    public void updateBarcodeReaderModel() {
        SchedulersCPPK.rfidExecutorService().execute(() -> {
            String[] model = new String[1];
            Dagger.appComponent().barcodeReader().getModel(model);
            runOnUiThread(() -> {
                setValue(barcodeReaderModel, model[0]);
            });
        });
    }

    public void updateBscReaderModel() {
        SchedulersCPPK.rfidExecutorService().execute(() -> {
            String[] model = new String[1];
           Dagger.appComponent().rfid().getModel(model);
            runOnUiThread(() -> {
                setValue(bscReaderModel, model[0]);
            });
        });
    }
}
