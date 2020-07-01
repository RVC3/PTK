package ru.ppr.chit.rfid;

import javax.inject.Inject;

import ru.ppr.chit.domain.BuildConfig;
import ru.ppr.chit.helpers.FilePathProvider;
import ru.ppr.core.domain.model.RfidType;
import ru.ppr.core.manager.RfidManager;
import ru.ppr.rfid.image.RfidImage;
import ru.ppr.rfidreal.RfidReal;

/**
 * Провайдер конфигурации для {@link RfidManager}.
 *
 * @author Aleksandr Brazhkin
 */
public class RfidManagerConfigProvider implements RfidManager.ConfigProvider {

    /**
     * Задержка автоматического выключения ридера после обращения к нему, ms
     */
    private static final long AUTO_POWER_OFF_DELAY = 10000;

    private final FilePathProvider filePathProvider;

    @Inject
    RfidManagerConfigProvider(FilePathProvider filePathProvider) {
        this.filePathProvider = filePathProvider;
    }

    @Override
    public RfidReal.Config getRfidRealConfig() {
        return new RfidReal.Config(AUTO_POWER_OFF_DELAY, true);
    }

    @Override
    public RfidImage.Config getRfidImageConfig() {
        return new RfidImage.Config(filePathProvider.getRfidImageDir());
    }

    @Override
    public RfidType getDefaultRfidType() {
        return BuildConfig.USE_REAL_DEVICES_BY_DEFAULT ? RfidType.REAL : RfidType.FILE;
    }

}
