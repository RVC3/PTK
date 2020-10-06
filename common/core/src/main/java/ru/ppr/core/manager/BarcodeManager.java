package ru.ppr.core.manager;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.barcode.file.BarcodeReaderFile;
import ru.ppr.barcodereal.BarcodeReaderI9000S;
import ru.ppr.barcodereal.BarcodeReaderMDI3100;
import ru.ppr.core.domain.model.BarcodeType;
import ru.ppr.logger.Logger;

/**
 * Менеджер сканера ШК.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class BarcodeManager {

    private static final String TAG = Logger.makeLogTag(BarcodeManager.class);

    public static final ExecutorService BARCODE_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(r -> new Thread(r, "barcode-thread"));

    private final Context context;
    private final ScreenManager screenManager;
    private final ConfigProvider configProvider;
    private IBarcodeReader reader;

    @Inject
    public BarcodeManager(Context context,
                          ScreenManager screenManager,
                          ConfigProvider configProvider) {
        this.context = context;
        this.screenManager = screenManager;
        this.configProvider = configProvider;
        screenManager.addScreenStateListener(screenStateListener);

        updateBarcode(configProvider.getDefaultBarcodeType());

        Logger.trace(TAG, "BarcodeManager initialized");
    }

    private void destroy() {
        screenManager.removeScreenStateListener(screenStateListener);
        Logger.trace(TAG, "BarcodeManager destroyed");
    }

    public IBarcodeReader getBarcodeReader() {
        return reader;
    }

    public void updateBarcode(BarcodeType barcodeType) {
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }

            Logger.trace(TAG, "updateBarcode(), mode = " + barcodeType);

            switch (barcodeType) {
                case FILE:
                    reader = new BarcodeReaderFile(configProvider.getBarcodeReaderFileConfig());
                    break;
                case MDI3100:
                    reader = new BarcodeReaderMDI3100(context, configProvider.getBarcodeReaderMDI3100Config(), BARCODE_EXECUTOR_SERVICE);
                    break;
                case I9000S:
                    reader = new BarcodeReaderI9000S(context, configProvider.getBarcodeReaderMDI3100Config(), BARCODE_EXECUTOR_SERVICE);
                    break;
                default:
                    throw new IllegalStateException("Incorrect barcode type");
            }
        } catch (Exception e) {
            Logger.error(TAG, e);
            throw new IllegalStateException("Can not create instance rfid");
        }
    }

    private ScreenManager.ScreenStateListener screenStateListener = screenOn -> {
        if (!screenOn) {
            if (reader != null) {
                BARCODE_EXECUTOR_SERVICE.execute(() -> getBarcodeReader().close());
            }
        }
    };

    /**
     * Провайдер конфигурации для {@link BarcodeManager}.
     */
    public interface ConfigProvider {
        BarcodeReaderMDI3100.Config getBarcodeReaderMDI3100Config();

        BarcodeReaderFile.Config getBarcodeReaderFileConfig();

        BarcodeType getDefaultBarcodeType();
    }
}
