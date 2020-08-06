package ru.ppr.cppk.managers;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Callable;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.CashRegister;
import ru.ppr.cppk.logic.CashRegisterValidityChecker;
import ru.ppr.cppk.logic.EklzChecker;
import ru.ppr.cppk.logic.FnSerialChecker;
import ru.ppr.cppk.logic.ShtrihFnSerialChecker;
import ru.ppr.cppk.logic.StubEklzChecker;
import ru.ppr.cppk.logic.StubFnSerialChecker;
import ru.ppr.cppk.logic.ZebraEklzChecker;
import ru.ppr.cppk.model.BluetoothDevice;
import ru.ppr.cppk.printer.PrinterFactory;
import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.base.OperationFactory;
import ru.ppr.cppk.printer.rx.operation.base.ShtrihOperationFactory;
import ru.ppr.cppk.printer.rx.operation.base.ZebraOperationFactory;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ObjectUtils;
import rx.Single;

/**
 * Менеджер для работы с принтером.
 *
 * @author Aleksandr Brazhkin
 */
public class PrinterManager {

    private static final String TAG = Logger.makeLogTag(PrinterManager.class);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PRINTER_MODE_FILE,
            PRINTER_MODE_MOEBIUS_REAL,
            PRINTER_MODE_MOEBIUS_VIRTUAL_EKLZ,
            PRINTER_MODE_SHTRIH})
    public @interface PrinterMode {
    }

    /**
     * Значения констант оставлены без изменений для обратной совместимости.
     */
    /**
     * Зебра Moebius SDK
     */
    public static final int PRINTER_MODE_MOEBIUS_REAL = 3;
    /**
     * Печать в файл
     */
    public static final int PRINTER_MODE_FILE = 4;
    /**
     * Зебра Moebius SDK c виртуальной ЭКЛЗ
     */
    public static final int PRINTER_MODE_MOEBIUS_VIRTUAL_EKLZ = 5;
    /**
     * Штрих
     */
    public static final int PRINTER_MODE_SHTRIH = 6;
    /**
    * Встроенный в UBX i9000S принтер
     */
    public static final int PRINTER_MODE_BUILTIN = 7;

    private final Globals globals;
    private IPrinter printer;
    private OperationFactory operationFactory;
    private EklzChecker eklzChecker;
    private BluetoothDevice bluetoothDevice;
    private int printerMode;
    private CashRegister cashRegister;
    private FnSerialChecker fnSerialChecker;

    /**
     * Фабрика принтера.
     */
    private final PrinterFactory printerFactory;
    /**
     * Хранилище Частных настроек ПТК
     */
    private final PrivateSettingsHolder privateSettingsHolder;

    public PrinterManager(Globals globals, PrinterFactory printerFactory, PrivateSettingsHolder privateSettingsHolder) {
        this.globals = globals;
        this.printerFactory = printerFactory;
        this.privateSettingsHolder = privateSettingsHolder;

        //инициализируем по последним настройкам
        updateConfig();

        Logger.trace(TAG, "PrinterManager initialized");
    }

    public void updateConfig() {
        Logger.trace(TAG, "updateConfig() START");
        //инициализируем по последним настройкам
        String macAddress = SharedPreferencesUtils.getPrinterMacAddress(globals);

        if (macAddress != null) {
            bluetoothDevice = new BluetoothDevice(macAddress, null);
        }

        printerMode = SharedPreferencesUtils.getPrinterMode(globals);
        cashRegister = SharedPreferencesUtils.getCashRegister(globals);

        updatePrinter();
        Logger.trace(TAG, "updateConfig() FINISH");
    }

    private void destroy() {
        Logger.trace(TAG, "PrinterManager destroyed");
    }

    public IPrinter getPrinter() {
        Logger.trace(TAG, "getPrinter mode = " + this.getPrinterMode());
        return printer;
    }

    public OperationFactory getOperationFactory() {
        return operationFactory;
    }

    public EklzChecker getEklzChecker() {
        return eklzChecker;
    }

    public void updatePrinter() {
        try {
            if (printer != null) {
                printer.terminate();
                printer = null;
            }

            @PrinterMode int mode = getPrinterMode();

            Logger.trace(TAG, "updatePrinter(), mode = " + mode);

            printer = printerFactory.getPrinter(mode, getPrinterMacAddress());
            PrinterResourcesManager printerResourcesManager = new PrinterResourcesManager(printer, privateSettingsHolder);

            switch (mode) {
                case PRINTER_MODE_MOEBIUS_REAL: {
                    operationFactory = new ZebraOperationFactory(printer, new TextFormatter(printer), printerResourcesManager);
                    eklzChecker = new ZebraEklzChecker(Di.INSTANCE.getCashierSessionInfo(), this);
                    fnSerialChecker = new StubFnSerialChecker();
                    break;
                }
                case PRINTER_MODE_FILE: {
                    operationFactory = new ZebraOperationFactory(printer, new TextFormatter(printer), printerResourcesManager);
                    eklzChecker = new ZebraEklzChecker(Di.INSTANCE.getCashierSessionInfo(), this);
                    fnSerialChecker = new StubFnSerialChecker();
                    break;
                }
                case PRINTER_MODE_MOEBIUS_VIRTUAL_EKLZ: {
                    operationFactory = new ZebraOperationFactory(printer, new TextFormatter(printer), printerResourcesManager);
                    eklzChecker = new ZebraEklzChecker(Di.INSTANCE.getCashierSessionInfo(), this);
                    fnSerialChecker = new StubFnSerialChecker();
                    break;
                }
                case PRINTER_MODE_BUILTIN: {
                    operationFactory = new ZebraOperationFactory(printer, new TextFormatter(printer), printerResourcesManager);
                    eklzChecker = new ZebraEklzChecker(Di.INSTANCE.getCashierSessionInfo(), this);
                    fnSerialChecker = new StubFnSerialChecker();
                    break;
                }
                case PRINTER_MODE_SHTRIH: {
                    operationFactory = new ShtrihOperationFactory(printer, new TextFormatter(printer), printerResourcesManager);
                    eklzChecker = new StubEklzChecker();
                    fnSerialChecker = new ShtrihFnSerialChecker(this);
                    break;
                }
            }
        } catch (Exception e) {
            Logger.error(TAG, e);
            throw new IllegalStateException("Can not create instance printer");
        }
    }

    public String getPrinterMacAddress() {
        return bluetoothDevice == null ? null : bluetoothDevice.getAddress();
    }

    public String getPrinterName() {
        return bluetoothDevice == null ? null : bluetoothDevice.getName();
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public CashRegister getCashRegister() {
        return cashRegister;
    }

    public int getPrinterMode() {
        return printerMode;
    }

    /**
     * Задает драйвер принтера без проверок, использовать только для Debug
     *
     * @param printerMode драйвер принтера
     */
    public void setPrinterMode(int printerMode) {
        SharedPreferencesUtils.setPrinterMode(globals, printerMode);

        this.printerMode = SharedPreferencesUtils.getPrinterMode(globals);
    }

    public void setCashRegister(CashRegister cashRegister) {
        Logger.trace(TAG, "CashRegister old " + this.cashRegister.toString());
        SharedPreferencesUtils.setCashRegister(globals, cashRegister);
        this.cashRegister = SharedPreferencesUtils.getCashRegister(globals);
        Logger.trace(TAG, "CashRegister new " + this.cashRegister.toString());
    }

    public Single<Void> changePrinterMacAddressAndMode(String macAddress, int printerMode) {
        return Single
                .fromCallable((Callable<Void>) () -> {
                    setTempMacAddressAndPrinterMode(macAddress, printerMode);
                    return null;
                })
                .observeOn(SchedulersCPPK.printer())
                .flatMap(aVoid -> getCashRegisterFromPrinter(printer))
                .flatMap(cashRegisterFromPrinter -> Single.fromCallable((Callable<Void>) () -> {
                    SharedPreferencesUtils.setPrinterMacAddress(globals, macAddress);
                    Logger.trace(TAG, "MacAddress applied " + macAddress);
                    SharedPreferencesUtils.setPrinterMode(globals, printerMode);
                    Logger.trace(TAG, "PrinterMode applied " + printerMode);

                    //http://agile.srvdev.ru/browse/CPPKPP-34182
                    //Если принтер тот же, но с другим ЭКЛЗ
                    if (!getEklzChecker().check(cashRegisterFromPrinter.getEKLZNumber(), cashRegisterFromPrinter.getSerialNumber())) {
                        //ничего не делаем, дальше отработает механизм проверки ЭКЛЗ при печати
                    } else {
                        setCashRegister(cashRegisterFromPrinter);

                        //если уже авторизован какой-то пользователь, то создади CashRegisterEvent
                        if (Di.INSTANCE.getCashierSessionInfo().getCurrentCashier() != null) {
                            CashRegister cashRegister = Di.INSTANCE.printerManager().getCashRegister();
                            Dagger.appComponent().cashRegisterEventCreator()
                                    .setCashRegister(cashRegister)
                                    .create();
                            Logger.trace(TAG, "CashRegisterEvent created");
                        } else {
                            Logger.info(TAG, "Пропускаем создание CashRegisterEvent т.к. пользователь не авторизован!");
                        }
                    }

                    return null;
                }))
                .doOnError(throwable -> restoreRealMacAddressAndPrinterMode());
    }

    /**
     * Deprecated, use {@link #changePrinterMacAddressAndMode(String, int)}
     */
    @Deprecated
    public Single<Void> changePrinterMacAddress(String macAddress) {
        return Single
                .fromCallable((Callable<Void>) () -> {
                    setTempMacAddress(macAddress);
                    return null;
                })
                .observeOn(SchedulersCPPK.printer())
                .flatMap(aVoid -> getCashRegisterFromPrinter(printer))
                .flatMap(cashRegisterFromPrinter -> Single.fromCallable((Callable<Void>) () -> {

                    SharedPreferencesUtils.setPrinterMacAddress(globals, macAddress);
                    Logger.trace(TAG, "MacAddress applied " + macAddress);

                    //http://agile.srvdev.ru/browse/CPPKPP-34182
                    //Если принтер тот же, но с другим ЭКЛЗ
                    if (!getEklzChecker().check(cashRegisterFromPrinter.getEKLZNumber(), cashRegisterFromPrinter.getSerialNumber())) {
                        //ничего не делаем, дальше отработает механизм проверки ЭКЛЗ при печати
                    } else {
                        setCashRegister(cashRegisterFromPrinter);

                        //если уже авторизован какой-то пользователь, то создади CashRegisterEvent
                        if (Di.INSTANCE.getCashierSessionInfo().getCurrentCashier() != null) {
                            CashRegister cashRegister = Di.INSTANCE.printerManager().getCashRegister();
                            Dagger.appComponent().cashRegisterEventCreator()
                                    .setCashRegister(cashRegister)
                                    .create();
                            Logger.trace(TAG, "CashRegisterEvent created");
                        } else {
                            Logger.info(TAG, "Пропускаем создание CashRegisterEvent т.к. пользователь не авторизован!");
                        }
                    }

                    return null;
                }))
                .doOnError(throwable -> {
                    restoreRealMacAddress();
                });
    }

    public void setTempMacAddressAndPrinterMode(String macAddress, int printerMode) {
        Logger.trace(TAG, "Used printer uninitialized with macAddress: " + getPrinterMacAddress() + " and mode: " + printerMode);

        BluetoothDevice newBluetoothDevice = new BluetoothDevice(macAddress, null);

        if (!ObjectUtils.equals(newBluetoothDevice, bluetoothDevice) || this.printerMode != printerMode) {
            this.bluetoothDevice = newBluetoothDevice;
            this.printerMode = printerMode;

            updatePrinter();
        }

        Logger.trace(TAG, "Temp printer initialized with macAddress: " + macAddress + " and mode: " + printerMode);
    }

    /**
     * Deprecated, use {@link #setTempMacAddressAndPrinterMode(String, int)}
     */
    @Deprecated
    public void setTempMacAddress(String macAddress) {
        Logger.trace(TAG, "Used printer uninitialized with macAddress " + getPrinterMacAddress());
        BluetoothDevice newBluetoothDevice = new BluetoothDevice(macAddress, null);
        if (!ObjectUtils.equals(newBluetoothDevice, bluetoothDevice)) {
            bluetoothDevice = newBluetoothDevice;
            updatePrinter();
        }
        Logger.trace(TAG, "Temp printer initialized with macAddress " + macAddress);
    }

    public void restoreRealMacAddressAndPrinterMode() {
        Logger.trace(TAG, "Temp printer uninitialized on error");

        BluetoothDevice newBluetoothDevice = null;
        String macAddress = SharedPreferencesUtils.getPrinterMacAddress(globals);
        int printerMode = SharedPreferencesUtils.getPrinterMode(globals);

        if (macAddress != null) {
            newBluetoothDevice = new BluetoothDevice(macAddress, null);
        }

        if (!ObjectUtils.equals(newBluetoothDevice, bluetoothDevice) || this.printerMode != printerMode) {
            this.bluetoothDevice = newBluetoothDevice;
            this.printerMode = printerMode;

            updatePrinter();
        }

        Logger.trace(TAG, "Used printer reinitialized with macAddress " + getPrinterMacAddress());
    }

    /**
     * Deprecated, use {@link #restoreRealMacAddressAndPrinterMode}
     */
    @Deprecated
    public void restoreRealMacAddress() {
        Logger.trace(TAG, "Temp printer uninitialized on error");
        BluetoothDevice newBluetoothDevice = null;
        String macAddress = SharedPreferencesUtils.getPrinterMacAddress(globals);
        if (macAddress != null) {
            newBluetoothDevice = new BluetoothDevice(macAddress, null);
        }
        if (!ObjectUtils.equals(newBluetoothDevice, bluetoothDevice)) {
            bluetoothDevice = newBluetoothDevice;
            updatePrinter();
        }
        Logger.trace(TAG, "Used printer reinitialized with macAddress " + getPrinterMacAddress());
    }

    public Single<CashRegister> getCashRegisterFromPrinter(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory().getGetStateOperation()
                .call()
                .toSingle()
                .flatMap(result -> Single.fromCallable(() -> {
                    CashRegister cashRegister = new CashRegister();
                    cashRegister.setINN(result.getINN());
                    cashRegister.setEKLZNumber(result.getEKLZNumber());
                    cashRegister.setFNSerial(result.getFNSerial());
                    cashRegister.setSerialNumber(result.getRegNumber());
                    cashRegister.setModel(result.getModel());
                    Logger.trace(TAG, "CashRegister from printer " + cashRegister.toString());
                    return cashRegister;
                }))
                .flatMap(cashRegister -> Single.fromCallable(() -> {
                    if (new CashRegisterValidityChecker().isValid(cashRegister)) {
                        Logger.trace(TAG, "CashRegister is valid");
                        return cashRegister;
                    } else {
                        Logger.trace(TAG, "CashRegister is not valid");
                        throw new Exception("Incorrect CashRegister " + cashRegister.toString());
                    }
                }));
    }

    public boolean checkFnSerial(String fnSerialFromPrinter) {
        return fnSerialChecker.check(fnSerialFromPrinter);
    }
}
