package ru.ppr.chit.rfid;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.AppProperties;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.core.domain.model.RfidType;
import ru.ppr.core.manager.RfidManager;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ObjectUtils;

/**
 * Синхронизатор конфигурации {@link RfidManager} с {@link AppProperties}.
 *
 * @author Dmitry Nevolin
 */
public class RfidManagerConfigSynchronizer {

    private static final String TAG = Logger.makeLogTag(RfidManagerConfigSynchronizer.class);

    private final AppPropertiesRepository appPropertiesRepository;
    private final RfidManager rfidManager;
    private RfidType prevRfidType;

    @Inject
    RfidManagerConfigSynchronizer(AppPropertiesRepository appPropertiesRepository,
                                         RfidManager rfidManager) {
        this.appPropertiesRepository = appPropertiesRepository;
        this.rfidManager = rfidManager;
    }

    public void init() {
        appPropertiesRepository
                .rxLoad()
                .observeOn(AppSchedulers.background())
                .map(appProperties -> {
                    RfidType rfidType = appProperties.getRfidType();
                    Logger.trace(TAG, "prev=" + prevRfidType + " | " + "new=" + rfidType);
                    return rfidType;
                })
                .filter(rfidType -> !ObjectUtils.equals(prevRfidType, rfidType))
                .subscribe(rfidType -> {
                            prevRfidType = rfidType;
                            rfidManager.updateRfid(rfidType);
                        }, throwable -> Logger.error(TAG, throwable)
                );
    }

}
