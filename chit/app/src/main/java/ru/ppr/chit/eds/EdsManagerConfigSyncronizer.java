package ru.ppr.chit.eds;

import java.util.EnumSet;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.AppProperties;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.helpers.FilePathProvider;
import ru.ppr.core.manager.eds.EdsConfig;
import ru.ppr.core.manager.eds.EdsDirs;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.edssft.LicType;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ObjectUtils;

/**
 * Синхронизатор конфигурации {@link EdsManager} с {@link AppProperties}.
 *
 * @author Aleksandr Brazhkin
 */
public class EdsManagerConfigSyncronizer {

    private static final String TAG = Logger.makeLogTag(EdsManagerConfigSyncronizer.class);

    private final EdsManager edsManager;
    private final AppPropertiesRepository appPropertiesRepository;
    private final FilePathProvider filePathProvider;
    private EdsConfig prevEdsConfig;

    @Inject
    EdsManagerConfigSyncronizer(EdsManager edsManager, AppPropertiesRepository appPropertiesRepository, FilePathProvider filePathProvider) {
        this.edsManager = edsManager;
        this.appPropertiesRepository = appPropertiesRepository;
        this.filePathProvider = filePathProvider;
    }

    public void init() {
        appPropertiesRepository
                .rxLoad()
                .observeOn(AppSchedulers.background())
                .map(appProperties -> {
                    Long deviceId = appProperties.getDeviceId();
                    Logger.trace(TAG, "prevType=" + (prevEdsConfig != null ? prevEdsConfig.edsType() : null) + " | " +
                            "newType=" + appProperties.getEdsType());
                    return new EdsConfig(
                            appProperties.getEdsType(),
                            deviceId == null ? 0 : deviceId,
                            new EdsDirs(filePathProvider.getSftDir(), filePathProvider.getSftUtilDir()),
                            EnumSet.of(LicType.CHECK)
                    );
                })
                .filter(edsConfig -> !ObjectUtils.equals(prevEdsConfig, edsConfig))
                .subscribe(edsConfig -> {
                            prevEdsConfig = edsConfig;
                            edsManager.updateConfig(edsConfig);
                        }, throwable -> Logger.error(TAG, throwable)
                );

    }
}
