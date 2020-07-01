package ru.ppr.cppk.dataCarrier.barcode;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import javax.inject.Inject;

import ru.ppr.barcode.file.BarcodeReaderFile;
import ru.ppr.barcodereal.BarcodeReaderMDI3100;
import ru.ppr.core.domain.model.BarcodeType;
import ru.ppr.core.manager.BarcodeManager;
import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.settings.SharedPreferencesUtils;

/**
 * Провайдер конфигурации для {@link BarcodeManager}.
 *
 * @author Aleksandr Brazhkin
 */
public class BarcodeManagerConfigProvider implements BarcodeManager.ConfigProvider {

    private final Context context;

    @Inject
    BarcodeManagerConfigProvider(Context context) {
        this.context = context;
    }

    @Override
    public BarcodeReaderMDI3100.Config getBarcodeReaderMDI3100Config() {
        int baudRate;
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        if (preferences.contains(GlobalConstants.OPTICON_BAUDRATE_SETTING_NAME)) {
            baudRate = Integer.valueOf(preferences.getString(GlobalConstants.OPTICON_BAUDRATE_SETTING_NAME, GlobalConstants.OPTICON_BAUDRATE_DEFAULT));
        } else {
            baudRate = Integer.valueOf(GlobalConstants.OPTICON_BAUDRATE_DEFAULT);
        }
        return new BarcodeReaderMDI3100.Config(GlobalConstants.AUTO_POWER_OFF_DELAY, true, baudRate);
    }

    @Override
    public BarcodeReaderFile.Config getBarcodeReaderFileConfig() {
        return new BarcodeReaderFile.Config(new File(PathsConstants.IMAGE_BARCODE));
    }

    @Override
    public BarcodeType getDefaultBarcodeType() {
        return SharedPreferencesUtils.getBarcodeType(context);
    }
}
