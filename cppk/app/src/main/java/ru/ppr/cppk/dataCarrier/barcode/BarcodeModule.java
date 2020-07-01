package ru.ppr.cppk.dataCarrier.barcode;

import dagger.Module;
import dagger.Provides;
import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.core.manager.BarcodeManager;

/**
 * @author Aleksandr Brazhkin
 */
@Module
public class BarcodeModule {

    @Provides
    BarcodeManager.ConfigProvider barcodeManagerConfigProvider(BarcodeManagerConfigProvider barcodeManagerConfigProvider) {
        return barcodeManagerConfigProvider;
    }

    @Provides
    IBarcodeReader barcodeReader(BarcodeManager barcodeManager) {
        return barcodeManager.getBarcodeReader();
    }
}
