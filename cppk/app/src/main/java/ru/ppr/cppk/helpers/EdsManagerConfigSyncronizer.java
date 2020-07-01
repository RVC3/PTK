package ru.ppr.cppk.helpers;

import android.content.Context;

import java.util.EnumSet;

import javax.inject.Inject;

import ru.ppr.core.manager.eds.EdsConfig;
import ru.ppr.core.manager.eds.EdsDirs;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.edssft.LicType;
import ru.ppr.logger.Logger;

/**
 * Синхронизатор конфигурации {@link EdsManager} с настройками приложения.
 *
 * @author Aleksandr Brazhkin
 */
public class EdsManagerConfigSyncronizer {

    private static final String TAG = Logger.makeLogTag(EdsManagerConfigSyncronizer.class);

    private final EdsManager edsManager;
    private final Context context;
    private final PrivateSettingsHolder privateSettingsHolder;
    private final FilePathProvider filePathProvider;

    @Inject
    EdsManagerConfigSyncronizer(EdsManager edsManager, Context context, PrivateSettingsHolder privateSettingsHolder, FilePathProvider filePathProvider) {
        this.edsManager = edsManager;
        this.context = context;
        this.privateSettingsHolder = privateSettingsHolder;
        this.filePathProvider = filePathProvider;
    }

    public void sync() {
        EdsConfig edsConfig = new EdsConfig(
                SharedPreferencesUtils.getEdsType(context),
                privateSettingsHolder.get().getTerminalNumber(),
                new EdsDirs(filePathProvider.getSftDir(), filePathProvider.getSftUtilDir()),
                EnumSet.of(LicType.CHECK, LicType.SELL)
        );
        edsManager.updateConfig(edsConfig);
    }
}
