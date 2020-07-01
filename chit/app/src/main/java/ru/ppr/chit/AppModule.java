package ru.ppr.chit;

import android.content.Context;
import android.os.Build;
import android.os.Handler;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.ppr.chit.api.auth.OAuth2TokenStorage;
import ru.ppr.chit.barcode.BarcodeModule;
import ru.ppr.chit.bs.oauth2token.OAuth2TokenManager;
import ru.ppr.chit.bs.oauth2token.OAuth2TokenStorageImpl;
import ru.ppr.chit.data.DataModule;
import ru.ppr.chit.data.DataSingletonModule;
import ru.ppr.chit.data.db.DbMigrationManager;
import ru.ppr.chit.data.db.migration.DbMigrationManagerImpl;
import ru.ppr.chit.domain.model.local.AppProperties;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.rfid.RfidModule;
import ru.ppr.core.dataCarrier.DataCarrierModule;
import ru.ppr.core.domain.model.ApplicationInfo;
import ru.ppr.core.domain.model.DeviceInfo;
import ru.ppr.core.manager.BluetoothManagerDefault;
import ru.ppr.core.manager.IBluetoothManager;

/**
 * @author Dmitry Nevolin
 */
@Singleton
@Module(includes = {DataCarrierModule.class, RfidModule.class, BarcodeModule.class, DataSingletonModule.class, DataModule.class})
public class AppModule {

    private final App app;
    private final Handler handler;

    AppModule(App app, Handler handler) {
        this.app = app;
        this.handler = handler;
    }

    @Provides
    App app() {
        return app;
    }

    @Provides
    Context context() {
        return app;
    }

    @Provides
    Handler handler() {
        return handler;
    }

    @Provides
    ApplicationInfo applicationInfo() {
        return new ApplicationInfo(
                BuildConfig.DEBUG,
                BuildConfig.APPLICATION_ID,
                BuildConfig.BUILD_TYPE,
                BuildConfig.FLAVOR,
                BuildConfig.VERSION_CODE,
                BuildConfig.VERSION_NAME
        );
    }

    @Provides
    DeviceInfo deviceInfo() {
        return new DeviceInfo(
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                Build.DISPLAY,
                Build.BRAND,
                Build.MODEL
        );
    }

    @Singleton
    @Provides
    OAuth2TokenStorage OAuth2TokenStorage(OAuth2TokenManager oAuth2TokenManager) {
        return new OAuth2TokenStorageImpl(oAuth2TokenManager);
    }

    @Provides
    IBluetoothManager bluetoothManager(BluetoothManagerDefault bluetoothManager) {
        return bluetoothManager;
    }

    @Provides
    AppProperties appProperties(AppPropertiesRepository appPropertiesRepository) {
        return appPropertiesRepository.load();
    }

    @Provides
    DbMigrationManager dbMigrationManager(DbMigrationManagerImpl dbMigrationManager) {
        return dbMigrationManager;
    }

}
