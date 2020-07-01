package ru.ppr.cppk;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.ppr.core.dataCarrier.DataCarrierModule;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.NsiDataProvider;
import ru.ppr.core.helper.Toaster;
import ru.ppr.core.manager.BluetoothManagerDefault;
import ru.ppr.core.manager.IBluetoothManager;
import ru.ppr.cppk.Sounds.BeepPlayer;
import ru.ppr.cppk.dataCarrier.barcode.BarcodeModule;
import ru.ppr.cppk.dataCarrier.rfid.RfidModule;
import ru.ppr.cppk.dataCarrier.smartCard.findcardtask.NsiDataProviderImpl;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.LocalDbSessionManager;
import ru.ppr.cppk.db.local.repository.LocalDbVersionRepositoryImpl;
import ru.ppr.cppk.db.local.repository.UpdateEventRepositoryImpl;
import ru.ppr.cppk.db.local.repository.base.LocalDbTransactionImpl;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.AppToaster;
import ru.ppr.cppk.helpers.CashierSessionInfo;
import ru.ppr.cppk.helpers.CommonSettingsStorage;
import ru.ppr.cppk.helpers.DeviceSessionInfo;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.helpers.UserSessionInfo;
import ru.ppr.cppk.localdb.repository.LocalDbVersionRepository;
import ru.ppr.cppk.localdb.repository.UpdateEventRepository;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;
import ru.ppr.cppk.logic.CriticalNsiChecker;
import ru.ppr.cppk.logic.FiscalHeaderParamsBuilder;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.TestPdSaleDocumentFactory;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.managers.db.LocalDbManager;
import ru.ppr.cppk.managers.db.NsiDbManager;
import ru.ppr.cppk.managers.db.SecurityDbManager;
import ru.ppr.cppk.printer.rx.operation.base.OperationFactory;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.SecurityDbSessionManager;

/**
 * DI-модуль для приложения.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
@Module(includes = {DataCarrierModule.class, RfidModule.class, BarcodeModule.class})
public class AppModule {

    private final Di di;

    public AppModule(Di di) {
        this.di = di;
    }

    @Provides
    @Singleton
    public Globals app() {
        return di.getApp();
    }

    @Provides
    @Singleton
    public Context context() {
        return di.getApp();
    }

    @Provides
    @Singleton
    public NsiVersionManager nsiVersionManager() {
        return di.nsiVersionManager();
    }

    @Provides
    @Singleton
    LocalDbSessionManager localDbSessionManager() {
        return di.localDbManager();
    }

    @Provides
    public LocalDaoSession localDaoSession() {
        return di.localDaoSession().get();
    }

    @Provides
    @Singleton
    NsiDbSessionManager nsiDbSessionManager() {
        return di.nsiDbManager();
    }

    @Provides
    public NsiDaoSession nsiDaoSession() {
        return di.nsiDaoSession().get();
    }

    @Provides
    @Singleton
    LocalDbManager localDbManager() {
        return di.localDbManager();
    }

    @Provides
    @Singleton
    SecurityDbSessionManager securityDbSessionManager() {
        return di.securityDbManager();
    }

    @Provides
    public SecurityDaoSession securityDaoSession() {
        return di.securityDaoSession().get();
    }

    @Provides
    @Singleton
    public UiThread uiThread() {
        return Di.INSTANCE.uiThread();
    }

    @Provides
    @Singleton
    public PermissionChecker permissionChecker() {
        return Di.INSTANCE.permissionChecker();
    }

    @Provides
    @Singleton
    BeepPlayer beepPlayer(Globals app) {
        return BeepPlayer.getInstance(app);
    }

    @Provides
    @Singleton
    public PrivateSettingsHolder privateSettingsHolder() {
        return new PrivateSettingsHolder(Di.INSTANCE.getPrivateSettings());
    }

    @Provides
    public PrivateSettings privateSettings(PrivateSettingsHolder privateSettingsHolder) {
        return privateSettingsHolder.get();
    }

    @Provides
    public CommonSettings commonSettings(CommonSettingsStorage commonSettingsStorage) {
        return commonSettingsStorage.get();
    }

    @Provides
    public ShiftManager shiftManager() {
        return Di.INSTANCE.getShiftManager();
    }

    @Provides
    public PosManager posManager() {
        return Di.INSTANCE.getPosManager();
    }

    @Provides
    public EventBuilder eventBuilder() {
        return Di.INSTANCE.eventBuilder();
    }

    @Provides
    public CashierSessionInfo cashierSessionInfo() {
        return Di.INSTANCE.getCashierSessionInfo();
    }

    @Provides
    public DeviceSessionInfo deviceSessionInfo() {
        return Di.INSTANCE.getDeviceSessionInfo();
    }

    @Provides
    NsiDataProvider nsiDataProvider(NsiDataProviderImpl nsiDataProvider) {
        return nsiDataProvider;
    }

    @Provides
    public CriticalNsiChecker criticalNsiChecker() {
        return new CriticalNsiChecker(Di.INSTANCE.nsiVersionManager(), Di.INSTANCE.getUserSessionInfo(), Di.INSTANCE.permissionChecker());
    }

    @Provides
    public PrinterManager printerManager() {
        return Di.INSTANCE.printerManager();
    }

    @Provides
    public IBluetoothManager bluetoothManager(BluetoothManagerDefault bluetoothManager) {
        return bluetoothManager;
    }

    @Provides
    public OperationFactory operationFactory(PrinterManager printerManager) {
        return printerManager.getOperationFactory();
    }

    @Provides
    public Toaster toaster(Context context) {
        return new AppToaster(context);
    }

    @Provides
    FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder() {
        return Di.INSTANCE.fiscalHeaderParamsBuilder();
    }

    @Provides
    TestPdSaleDocumentFactory testPdSaleDocumentFactory() {
        return Di.INSTANCE.testPdSaleDocumentFactory();
    }

    @Provides
    LocalDbVersionRepository localDbVersionRepository(LocalDbVersionRepositoryImpl localDbVersionRepository) {
        return localDbVersionRepository;
    }

    @Provides
    UpdateEventRepository updateEventRepository(UpdateEventRepositoryImpl updateEventRepository) {
        return updateEventRepository;
    }

    @Provides
    LocalDbTransaction localDbTransaction(LocalDbTransactionImpl localDbTransaction) {
        return localDbTransaction;
    }

    @Provides
    NsiDbManager nsiDbManager() {
        return di.nsiDbManager();
    }


    @Provides
    SecurityDbManager securityDbManager() {
        return di.securityDbManager();
    }

    @Provides
    UserSessionInfo userSessionInfo() {
        return di.getUserSessionInfo();
    }

}
