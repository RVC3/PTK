package ru.ppr.chit.barcode;

import javax.inject.Inject;

import ru.ppr.barcode.file.BarcodeReaderFile;
import ru.ppr.barcodereal.BarcodeReaderMDI3100;
import ru.ppr.chit.domain.BuildConfig;
import ru.ppr.chit.helpers.FilePathProvider;
import ru.ppr.core.domain.model.BarcodeType;
import ru.ppr.core.manager.BarcodeManager;

/**
 * Провайдер конфигурации для {@link BarcodeManager}.
 *
 * @author Aleksandr Brazhkin
 */
public class BarcodeManagerConfigProvider implements BarcodeManager.ConfigProvider {

    /**
     * Задержка автоматического выключения ридера после обращения к нему, ms
     */
    private static final long AUTO_POWER_OFF_DELAY = 10000;

    private static final int BAUD_RATE_DEFAULT = 115200;

    private final FilePathProvider filePathProvider;

    @Inject
    BarcodeManagerConfigProvider(FilePathProvider filePathProvider) {
        this.filePathProvider = filePathProvider;
    }

    @Override
    public BarcodeReaderMDI3100.Config getBarcodeReaderMDI3100Config() {
        return new BarcodeReaderMDI3100.Config(AUTO_POWER_OFF_DELAY, true, BAUD_RATE_DEFAULT);
    }

    @Override
    public BarcodeReaderFile.Config getBarcodeReaderFileConfig() {
        return new BarcodeReaderFile.Config(filePathProvider.getBarcodeImageDir());
    }

    @Override
    public BarcodeType getDefaultBarcodeType() {
        return BuildConfig.USE_REAL_DEVICES_BY_DEFAULT ? BarcodeType.MDI3100 : BarcodeType.FILE;
    }
}
