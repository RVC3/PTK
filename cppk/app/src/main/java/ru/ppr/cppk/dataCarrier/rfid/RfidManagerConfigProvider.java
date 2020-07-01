package ru.ppr.cppk.dataCarrier.rfid;

import android.content.Context;

import java.io.File;

import javax.inject.Inject;

import ru.ppr.core.domain.model.RfidType;
import ru.ppr.core.manager.RfidManager;
import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.rfid.image.RfidImage;
import ru.ppr.rfidreal.RfidReal;

/**
 * Провайдер конфигурации для {@link RfidManager}.
 *
 * @author Aleksandr Brazhkin
 */
public class RfidManagerConfigProvider implements RfidManager.ConfigProvider {

    private final Context context;

    @Inject
    RfidManagerConfigProvider(Context context) {
        this.context = context;
    }

    @Override
    public RfidReal.Config getRfidRealConfig() {
        return new RfidReal.Config(GlobalConstants.AUTO_POWER_OFF_DELAY, true);
    }

    @Override
    public RfidImage.Config getRfidImageConfig() {
        return new RfidImage.Config(new File(PathsConstants.IMAGE_RFID));
    }

    @Override
    public RfidType getDefaultRfidType() {
        return SharedPreferencesUtils.getRfidType(context);
    }
}
