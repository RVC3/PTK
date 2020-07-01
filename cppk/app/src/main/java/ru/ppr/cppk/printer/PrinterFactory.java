package ru.ppr.cppk.printer;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;

import ru.ppr.core.manager.IBluetoothManager;
import ru.ppr.core.manager.network.NetworkManager;
import ru.ppr.cppk.InternalPrinter9000S;
import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.helpers.CommonSettingsStorage;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.managers.handler.PrinterShtrihBluetoothHandler;
import ru.ppr.cppk.managers.handler.PrinterShtrihNetworkHandler;
import ru.ppr.cppk.managers.handler.PrinterZebraMoebiusBluetoothHandler;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.file.PrinterFile;
import ru.ppr.ikkm.file.db.PrinterDaoSession;
import ru.ppr.ikkm.file.db.PrinterSQLiteHelper;
import ru.ppr.ikkm.file.state.storage.DbStateStorage;
import ru.ppr.logger.Logger;
import ru.ppr.moebius.PrinterZebraMoebius;
import ru.ppr.moebius.PrinterZebraMoebiusVirtualEklz;
import ru.ppr.shtrih.PrinterShtrih;
import ru.ppr.shtrih.SyncChecker;
import ru.ppr.utils.FileUtils2;

/**
 * Фабрика принтера.
 *
 * @author Aleksandr Brazhkin
 */
public class PrinterFactory {

    private static final String TAG = Logger.makeLogTag(PrinterFactory.class);

    /**
     * Имя файла с настроечной таблицей в assets для Штриха
     */
    private static final String ADJUSTING_TABLE_FILE_NAME = "AdjustingTable.bmp";

    private final Context context;
    private final IBluetoothManager bluetoothManager;
    private final NetworkManager networkManager;
    private final CommonSettingsStorage commonSettingsStorage;

    public PrinterFactory(Context context, IBluetoothManager bluetoothManager, NetworkManager networkManager, CommonSettingsStorage commonSettingsStorage) {
        this.context = context;
        this.bluetoothManager = bluetoothManager;
        this.networkManager = networkManager;
        this.commonSettingsStorage = commonSettingsStorage;
    }

    @NonNull
    public IPrinter getPrinter(@PrinterManager.PrinterMode int printerMode, String macAddress) throws Exception {
        switch (printerMode) {
            case PrinterManager.PRINTER_MODE_MOEBIUS_REAL: {
                return new PrinterZebraMoebius(
                        context,
                        new File(PathsConstants.LOG_ZEBRA),
                        macAddress,
                        new PrinterZebraMoebiusBluetoothHandler(bluetoothManager)
                );
            }
            case PrinterManager.PRINTER_MODE_FILE: {
                PrinterSQLiteHelper sqLiteOpenHelper = new PrinterSQLiteHelper(context);
                DbStateStorage dbStateStorage = new DbStateStorage(new PrinterDaoSession(sqLiteOpenHelper), PrinterFile.PRINTER_FILE_ID, PrinterFile.MODEl);
                return new PrinterFile(
                        new File(PathsConstants.PRINTER),
                        dbStateStorage
                );
            }
            case PrinterManager.PRINTER_MODE_BUILTIN: {
                return new InternalPrinter9000S(
                        context,
                        new File(PathsConstants.LOG_i9000S_print),
                        macAddress
                );
            }
            case PrinterManager.PRINTER_MODE_MOEBIUS_VIRTUAL_EKLZ: {
                return new PrinterZebraMoebiusVirtualEklz(
                        context,
                        new File(PathsConstants.LOG_ZEBRA),
                        macAddress,
                        new PrinterZebraMoebiusBluetoothHandler(bluetoothManager)
                );
            }
            case PrinterManager.PRINTER_MODE_SHTRIH: {
                return new PrinterShtrih(
                        context,
                        Dagger.appComponent().filePathProvider().getShtrihLogsDir(),
                        Dagger.appComponent().filePathProvider().getShtrihWorkingDir(),
                        dst -> FileUtils2.copyFileFromAssets(context, ADJUSTING_TABLE_FILE_NAME, dst),
                        macAddress,
                        new PrinterShtrihBluetoothHandler(bluetoothManager),
                        new PrinterShtrihNetworkHandler(networkManager),
                        new SyncChecker(new SyncChecker.ConfigProvider() {
                            @Override
                            public int getCountTrigger() {
                                return commonSettingsStorage.get().getPrinterSendToOfdCountTrigger();
                            }

                            @Override
                            public int getPeriodTrigger() {
                                return commonSettingsStorage.get().getPrinterSendToOfdPeriodTrigger();
                            }
                        }));
            }
            default: {
                throw new IllegalArgumentException("Unknown printer type");
            }
        }
    }
}
