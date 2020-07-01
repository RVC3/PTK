package ru.ppr.edssft.real;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.TimeZone;

import ru.ppr.edssft.LicType;
import ru.ppr.edssft.SftEdsChecker;
import ru.ppr.edssft.model.GetKeyInfoResult;
import ru.ppr.edssft.model.GetStateResult;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.edssft.model.VerifySignResult;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Контроллер ЭЦП, основанный на SFT.
 *
 * @author Aleksandr Brazhkin
 */
public class RealSftEdsChecker implements SftEdsChecker {

    private static final String TAG = Logger.makeLogTag(RealSftEdsChecker.class);

    private static final String CONFIG_FILE_NAME = "sftsdk3config.xml";
    private static final String SFT_ASSETS_DIR = "sftutil";
    private static final String SFT_UTIL_NAME = "lic_support.android";
    private static final String SFT_UTIL_LIB_NAME = "libcryptoc.so";

    /**
     * Менеджер для включения/выключения Bluetooth
     */
    private final BluetoothManager bluetoothManager;
    private final NativeSft nativeSft;
    private final Context context;
    private final long userId;
    private final File workingDir;
    private final File transportDir;
    private final File inDir;
    private final File outDir;
    private File sftUtilDir;
    private File utilFile;
    private File utilLibFile;
    private File utilSrcDir;
    private File utilDstDir;

    private boolean opened = false;

    public RealSftEdsChecker(Context context,
                             BluetoothManager bluetoothManager,
                             long userId,
                             File workingDir,
                             File transportDir,
                             File inDir,
                             File outDir,
                             File sftUtilDir,
                             File utilSrcDir,
                             File utilDstDir) {
        this.nativeSft = new NativeSft();
        this.context = context;
        this.bluetoothManager = bluetoothManager;
        this.userId = userId;
        this.workingDir = workingDir;
        this.transportDir = transportDir;
        this.inDir = inDir;
        this.outDir = outDir;
        this.sftUtilDir = sftUtilDir;
        this.utilFile = new File(sftUtilDir, SFT_UTIL_NAME);
        this.utilLibFile = new File(sftUtilDir, SFT_UTIL_LIB_NAME);
        this.utilSrcDir = utilSrcDir;
        this.utilDstDir = utilDstDir;
    }

    @Override
    public boolean open() {
        if (!opened) {
            Logger.trace(TAG, "open: Opening processor");
            if (!createConfigFileIfNeed()) {
                Logger.trace(TAG, "open: Could not create config file");
                return false;
            }
            Logger.trace(TAG, "open: Deleting old requests before opening processor");
            FileUtils2.clearDir(outDir, null);
            int res = nativeSft.nativeOpenProcessor(workingDir.getAbsolutePath(), transportDir.getAbsolutePath());
            Logger.trace(TAG, "open: nativeOpenProcessor called, res = " + res);
            if (res == 0) {
                opened = true;
                Logger.trace(TAG, "open: Processor successfully opened");
            } else {
                Logger.trace(TAG, "open: Error while opening processor");
            }
        } else {
            Logger.trace(TAG, "open: Processor is already opened");
        }
        return opened;
    }

    @Override
    public boolean close() {
        if (opened) {
            Logger.trace(TAG, "close: Closing processor");
            int res = nativeSft.nativeCloseProcessor();
            Logger.trace(TAG, "close: nativeCloseProcessor called, res = " + res);
            if (res == 0) {
                opened = false;
                Logger.trace(TAG, "close: Processor successfully closed");
            } else {
                Logger.trace(TAG, "close: Error while closing processor");
            }
        } else {
            Logger.trace(TAG, "close: Processor is already closed");
        }
        return !opened;
    }

    @NonNull
    @Override
    public SignDataResult signData(byte[] data, Date signDateTime) {

        Logger.trace(TAG, "signData: start");

        SignDataResult signDataResult = new SignDataResult();

        nativeSft.setSignData(null);
        nativeSft.setKeyNumber(0);

        final boolean checkState = openAndCheckState(false, true);
        if (checkState) {
            long timeForInfotecs = signDateTime.getTime();
            // В будущем Костыль, выпилить, когда инфотекс пофиксит баг
            // По предположению Андрея SDK добавляет посикс время к локальному вместо ютс, соответственно неправильно выбирается ключ.
            // Гриша, тебе нужно сделать так же, т.е. взять PosixUTC и перед передачей в SFT, прибавить к нему разницу текущего времени и UTC, в секундах
            //https://aj.srvdev.ru/browse/CPPKPP-25479
            int offset = TimeZone.getDefault().getOffset(timeForInfotecs);
            timeForInfotecs = timeForInfotecs + offset;

            int res = nativeSft.nativeSignData(data, timeForInfotecs / 1000);
            Logger.trace(TAG, "signData: nativeSft.nativeSignData, res = " + res);

            if (res == 0) {
                signDataResult.setSuccessful(true);
            } else {
                String errorMessage = nativeSft.nativeGetLastError();
                signDataResult.setDescription(errorMessage);
            }

            signDataResult.setSignature(nativeSft.getSignData());
            signDataResult.setEdsKeyNumber(nativeSft.getKeyNumber());
        }

        signDataResult.setData(data);
        signDataResult.setSignDateTime(signDateTime);

        Logger.trace(TAG, "signData: end, signDataResult = " + signDataResult);

        return signDataResult;
    }

    @NonNull
    @Override
    public GetKeyInfoResult getKeyInfo(long edsKeyNumber) {

        Logger.trace(TAG, "getKeyInfo: start");

        GetKeyInfoResult getKeyInfoResult = new GetKeyInfoResult();

        nativeSft.setKeyValidSince(0);
        nativeSft.setKeyValidTill(0);
        nativeSft.setKeyWhenRevocated(0);
        nativeSft.setDeviceId(null);

        if (openAndCheckState(true, false)) {
            int res = nativeSft.nativeGetKeyInfo(safeConvertLongToInt(edsKeyNumber));
            Logger.trace(TAG, "getKeyInfo: nativeSft.nativeGetKeyInfo, res = " + res);

            if (res == 0) {
                getKeyInfoResult.setSuccessful(true);
                Logger.trace(TAG, "getKeyInfo: nativeSft.KeyValidSince = " + nativeSft.getKeyValidSince());
                Logger.trace(TAG, "getKeyInfo: nativeSft.KeyValidTill = " + nativeSft.getKeyValidTill());
                Logger.trace(TAG, "getKeyInfo: nativeSft.KeyWhenRevocated = " + nativeSft.getKeyWhenRevocated());
            } else {
                String errorMessage = nativeSft.nativeGetLastError();
                getKeyInfoResult.setDescription(errorMessage);
            }

            getKeyInfoResult.setDeviceId(getLongFromHex(nativeSft.getDeviceId()));
            getKeyInfoResult.setEffectiveDate(new Date(nativeSft.getKeyValidSince() * 1000));
            getKeyInfoResult.setExpireDate(new Date(nativeSft.getKeyValidTill() * 1000));
            getKeyInfoResult.setDateOfRevocation(new Date(nativeSft.getKeyWhenRevocated() * 1000));
        }

        Logger.trace(TAG, "getKeyInfo: end, getKeyInfoResult = " + getKeyInfoResult);

        return getKeyInfoResult;
    }

    @NonNull
    @Override
    public VerifySignResult verifySign(byte[] data, byte[] signature, long edsKeyNumber) {

        Logger.trace(TAG, "verifySign: start");

        VerifySignResult verifySignResult = new VerifySignResult();

        if (openAndCheckState(true, false)) {
            int res = nativeSft.nativeVerifySign(data, signature, edsKeyNumber);
            Logger.trace(TAG, "verifySign: nativeSft.nativeVerifySign, res = " + res);
            if (res == 0) {
                verifySignResult.setSuccessful(true);
                verifySignResult.setSignValid(nativeSft.isSignValid());
            } else {
                String errorMessage = nativeSft.nativeGetLastError();
                verifySignResult.setDescription(errorMessage);
                verifySignResult.setSignValid(false);
            }
        }

        Logger.trace(TAG, "verifySign: end, verifySignResult = " + verifySignResult);

        return verifySignResult;
    }

    @Override
    public GetStateResult getState() {

        Logger.trace(TAG, "getState: start");

        GetStateResult getStateResult = new GetStateResult();

        if (userId <= 0) {
            Logger.trace(TAG, "getState: Invalid user id = " + userId);
            Logger.trace(TAG, "getState: end, getStateResult = " + getStateResult);
            return getStateResult;
        }

        int res = -1;
        if (opened) {

            nativeSft.setState(NativeSft.DEFAULT_SFT_STATE);
            res = nativeSft.nativeGetState();
            Logger.trace(TAG, "getState: nativeSft.nativeGetState, res = " + res);

            @State int sftState = nativeSft.getState();
            getStateResult.setState(sftState);

            if (res != 0) {
                String errorMessage = nativeSft.nativeGetLastError();
                Logger.trace(TAG, "getState: nativeGetState error: " + errorMessage);

                if (!close()) {
                    Logger.trace(TAG, "getState: end, getStateResult = " + getStateResult);
                    return getStateResult;
                }
            }
        }

        if (res != 0) {

            res = nativeSft.nativeSetUserId(safeConvertLongToInt(userId));
            Logger.trace(TAG, "getState: nativeSft.nativeSetUserId, res = " + res);

            if (res != 0) {
                String errorMessage = nativeSft.nativeGetLastError();
                Logger.trace(TAG, "getState: nativeSetUserId error: " + errorMessage);
                Logger.trace(TAG, "getState: end, getStateResult = " + getStateResult);
                return getStateResult;
            } else {
                if (!open()) {
                    Logger.trace(TAG, "getState: end, getStateResult = " + getStateResult);
                    return getStateResult;
                }

                nativeSft.setState(NativeSft.DEFAULT_SFT_STATE);
                res = nativeSft.nativeGetState();
                Logger.trace(TAG, "getState: nativeSft.nativeGetState, res = " + res);

                @State int sftState = nativeSft.getState();
                getStateResult.setState(sftState);

                if (res != 0) {
                    String errorMessage = nativeSft.nativeGetLastError();
                    Logger.trace(TAG, "getState: nativeGetState error: " + errorMessage);
                    Logger.trace(TAG, "getState: end, getStateResult = " + getStateResult);
                    return getStateResult;
                }
            }
        }

        @State int sftState = nativeSft.getState();
        getStateResult.setState(sftState);

        ////////////////////////////////////////////////////////////////
        if (sftState != SFT_STATE_ALL_LICENSES) {

            EnumSet<LicType> licTypes;

            if (sftState == SFT_STATE_NO_LICENSES) {
                licTypes = EnumSet.of(LicType.CHECK, LicType.SELL);
            } else if (sftState == SFT_STATE_ONLY_CHECK_LICENSE) {
                licTypes = EnumSet.of(LicType.SELL);
            } else if (sftState == SFT_STATE_ONLY_SELL_LICENSE) {
                licTypes = EnumSet.of(LicType.CHECK);
            } else {
                throw new IllegalArgumentException("Unsupported sftState = " + sftState);
            }

            if (SftSdkLicenseUtils.checkLicFileInWorkingFolder(outDir, licTypes)) {
                //файлы лицензий в папке есть, но сфт затупила
                //передернем сфт
                pullBluetooth();
                //выключим сфт
                close();
                open();
                //запросим еще раз state
                nativeSft.setState(NativeSft.DEFAULT_SFT_STATE);
                res = nativeSft.nativeGetState();
                Logger.trace(TAG, "getState: nativeSft.nativeGetState, res = " + res);

                @State int safeSftState = nativeSft.getState();
                getStateResult.setState(safeSftState);

                if (res != 0) {
                    String errorMessage = nativeSft.nativeGetLastError();
                    Logger.trace(TAG, "getState: nativeGetState error: " + errorMessage);
                    Logger.trace(TAG, "getState: end, getStateResult = " + getStateResult);
                    return getStateResult;
                }
            }
        }

        getStateResult.setSuccessful(true);

        Logger.trace(TAG, "getState: end, getStateResult = " + getStateResult);

        return getStateResult;
    }

    @Override
    public boolean takeLicenses() {
        if (!createUtilDirIfNeed()) {
            return false;
        }
        if (!createUtilFilesIfNeed()) {
            return false;
        }
        return SftSdkLicenseUtils.runTakeLicCommand(utilFile, utilLibFile, utilSrcDir, workingDir);
    }

    @Override
    public void createLicRequest(EnumSet<LicType> licTypes) {
        if (!createUtilDirIfNeed()) {
            return;
        }
        if (!createUtilFilesIfNeed()) {
            return;
        }
        close();
        SftSdkLicenseUtils.runLicRequestCommand(utilFile, utilLibFile, licTypes, utilDstDir, workingDir);
    }

    /**
     * Передергивает Bluetooth.
     */
    private void pullBluetooth() {
        Logger.trace(TAG, "pullBluetooth: start");
        bluetoothManager.disable();
        bluetoothManager.enable();
        Logger.trace(TAG, "pullBluetooth: end");
    }

    /**
     * Открывает SFT, и проверяет её готовность к работе.
     *
     * @param forCheck - проверить для возможности проверки
     * @param forSell  - проверить для возможности продажи
     * @return {@code true} при успешном выполнении операции, {@code false} иначе.
     */
    private boolean openAndCheckState(boolean forCheck, boolean forSell) {
        GetStateResult getStateResult = getState();
        boolean res = getStateResult.isSuccessful();
        if (res && forCheck) {
            res = (getStateResult.getState() == SftEdsChecker.SFT_STATE_ALL_LICENSES ||
                    getStateResult.getState() == SftEdsChecker.SFT_STATE_ONLY_CHECK_LICENSE);
        }
        if (res && forSell) {
            res = (getStateResult.getState() == SftEdsChecker.SFT_STATE_ALL_LICENSES ||
                    getStateResult.getState() == SftEdsChecker.SFT_STATE_ONLY_SELL_LICENSE);
        }
        Logger.trace(TAG, "openAndCheckState(forCheck=" + forCheck + ", forSell=" + forSell + "): res = " + res);
        return res;
    }

    /**
     * Копирует файл конфига в рабочую папку SFT.
     * Без этого файла SFT крашит приложение.
     *
     * @return {@code true} при успешном выполнении операции, {@code false} иначе.
     */
    private boolean createConfigFileIfNeed() {
        File configFile = new File(workingDir, CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            try {
                Logger.trace(TAG, "createConfigFileIfNeed: copying " + CONFIG_FILE_NAME + " from assets");
                FileUtils2.copyFileFromAssets(context, CONFIG_FILE_NAME, configFile);
            } catch (IOException e) {
                Logger.error(TAG, e);
            }
            if (!configFile.exists()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Создает папку для утилиты SFT.
     *
     * @return {@code true} при успешном выполнении операции, {@code false} иначе.
     */
    private boolean createUtilDirIfNeed() {
        if (!sftUtilDir.exists()) {
            Logger.trace(TAG, "createUtilDirIfNeed: Creating sftUtilDir: " + sftUtilDir.getAbsolutePath());
            if (!sftUtilDir.mkdirs()) {
                Logger.error(TAG, "createUtilDirIfNeed: Creating sftUtilDir failed");
                return false;
            }
        }
        return true;
    }

    /**
     * Создает папки src и dst, необходимые для работы утилиты SFT.
     *
     * @return {@code true} при успешном выполнении операции, {@code false} иначе.
     */
    private boolean createUtilFilesIfNeed() {
        if (!utilFile.exists()) {
            try {
                Logger.trace(TAG, "prepareSftUtil: copying utilFile from assets");
                FileUtils2.copyFileFromAssets(context, SFT_ASSETS_DIR + "/" + SFT_UTIL_NAME, utilFile);
                if (!utilFile.exists()) {
                    return false;
                }
            } catch (IOException e) {
                Logger.error(TAG, e);
                return false;
            }
        }
        if (!utilLibFile.exists()) {
            try {
                Logger.trace(TAG, "prepareSftUtil: copying utilLibFile from assets");
                FileUtils2.copyFileFromAssets(context, SFT_ASSETS_DIR + "/" + SFT_UTIL_LIB_NAME, utilLibFile);
                if (!utilLibFile.exists()) {
                    return false;
                }
            } catch (IOException e) {
                Logger.error(TAG, e);
                return false;
            }
        }

        boolean canBeExecuted = utilFile.canExecute() || utilFile.setExecutable(true, true);
        Logger.trace(TAG, "prepareSftUtil: canBeExecuted = " + canBeExecuted);

        return canBeExecuted;
    }

    /**
     * Выполняет конвертацию массива байт в long
     *
     * @param rawData Массив байт
     * @return Значение типа {@code long}
     */
    private long getLongFromHex(byte[] rawData) {
        long value = -1;
        if (rawData != null) {
            try {
                int intValue = java.nio.ByteBuffer.wrap(rawData).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                value = 0xffffffffL & intValue;
            } catch (Exception e) {
                Logger.error(TAG, e);
            }
        }
        return value;
    }

    /**
     * Выполняет безопасную конвертацию long в int
     *
     * @param value Значение типа {@code long}
     * @return Значение типа {@code int}
     */
    private int safeConvertLongToInt(long value) {
        if (value >> 32 != 0) {
            throw new IllegalArgumentException("Could not safe convert long to int");
        }
        int res = (int) value;
        return res;
    }

    /**
     * Менеджер для включения/выключения Bluetooth
     */
    public interface BluetoothManager {
        boolean enable();

        boolean disable();

        boolean isEnabled();
    }
}
