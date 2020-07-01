package ru.ppr.chit.barcode;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.AppProperties;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.rfid.RfidManagerConfigSynchronizer;
import ru.ppr.core.domain.model.BarcodeType;
import ru.ppr.core.manager.BarcodeManager;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ObjectUtils;

/**
 * Синхронизатор конфигурации {@link BarcodeManager} с {@link AppProperties}.
 *
 * @author Dmitry Nevolin
 */
public class BarcodeManagerConfigSynchronizer {

    private static final String TAG = Logger.makeLogTag(BarcodeManagerConfigSynchronizer.class);

    private final AppPropertiesRepository appPropertiesRepository;
    private final BarcodeManager barcodeManager;
    private BarcodeType prevBarcodeType;

    @Inject
    BarcodeManagerConfigSynchronizer(AppPropertiesRepository appPropertiesRepository,
                                     BarcodeManager barcodeManager) {
        this.appPropertiesRepository = appPropertiesRepository;
        this.barcodeManager = barcodeManager;
    }

    public void init() {
        appPropertiesRepository
                .rxLoad()
                .observeOn(AppSchedulers.background())
                .map(appProperties -> {
                    BarcodeType barcodeType = appProperties.getBarcodeType();
                    Logger.trace(TAG, "prev=" + prevBarcodeType + " | " + "new=" + barcodeType);
                    return barcodeType;
                })
                .filter(barcodeType -> !ObjectUtils.equals(prevBarcodeType, barcodeType))
                .subscribe(barcodeType -> {
                            prevBarcodeType = barcodeType;
                            barcodeManager.updateBarcode(barcodeType);
                        }, throwable -> Logger.error(TAG, throwable)
                );
    }

}
