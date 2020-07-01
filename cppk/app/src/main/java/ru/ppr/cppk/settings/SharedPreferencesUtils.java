package ru.ppr.cppk.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.core.domain.model.BarcodeType;
import ru.ppr.core.domain.model.EdsType;
import ru.ppr.core.domain.model.RfidType;
import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.Sounds.Ringtone.BeepType;
import ru.ppr.cppk.legacy.SamSlot;
import ru.ppr.cppk.localdb.model.CashRegister;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.pos.PosType;
import ru.ppr.logger.Logger;

public class SharedPreferencesUtils {

    private static final String TAG = Logger.makeLogTag(SharedPreferencesUtils.class);

    /**
     * Сохраняет текущий принтер в SP
     *
     * @param c
     * @param cashRegister - чековый принтер
     */
    public static void setCashRegister(Context c, CashRegister cashRegister) {
        SharedPreferences preferences = c.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(GlobalConstants.CashRegisterEklsNumber, cashRegister.getEKLZNumber());
        editor.putString(GlobalConstants.CashRegisterFnSerial, cashRegister.getFNSerial());
        editor.putString(GlobalConstants.CashRegisterInn, cashRegister.getINN());
        editor.putString(GlobalConstants.CashRegisterModel, cashRegister.getModel());
        editor.putString(GlobalConstants.CashRegisterSerialNumber, cashRegister.getSerialNumber());
        Logger.info(TAG, "setCashRegister(" + cashRegister.toString() + ")");
        editor.apply();
    }

    /**
     * Возвращает имя текущего пользователя или null
     *
     * @param c
     * @return
     */
    public static CashRegister getCashRegister(Context c) {
        SharedPreferences preferences = c.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        CashRegister cashRegister = new CashRegister();
        cashRegister.setINN(preferences.getString(GlobalConstants.CashRegisterInn, null));
        cashRegister.setEKLZNumber(preferences.getString(GlobalConstants.CashRegisterEklsNumber, null));
        cashRegister.setFNSerial(preferences.getString(GlobalConstants.CashRegisterFnSerial, null));
        cashRegister.setSerialNumber(preferences.getString(GlobalConstants.CashRegisterSerialNumber, null));
        cashRegister.setModel(preferences.getString(GlobalConstants.CashRegisterModel, null));
        Logger.trace(TAG, "getCashRegister(): " + cashRegister.toString());
        return cashRegister;
    }

    public static void createDefaultSettings(Context c) {
        SharedPreferences preferences = c.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);

        if (!preferences.contains(GlobalConstants.OPTICON_BAUDRATE_SETTING_NAME)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(GlobalConstants.OPTICON_BAUDRATE_SETTING_NAME, GlobalConstants.OPTICON_BAUDRATE_DEFAULT);
            editor.putBoolean(GlobalConstants.ENABLE_TOAST_SETTING, true);
            editor.putBoolean(GlobalConstants.ECP_CHECK_SETTINGS, true);
            editor.apply();
        }
    }

    /**
     * Активировны ли всплывающие сообщения об ошибках
     */
    public static boolean isErrorToastsEnabled(Context c) {
        SharedPreferences preferences = c.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalConstants.ENABLE_TOAST_SETTING, false);
    }

    /**
     * Разрешает/запрещает всплывающие сообщения об ошибках
     *
     * @param c
     * @param enable
     */
    public static void setErrorToastsEnable(Context c, boolean enable) {
        SharedPreferences preferences = c.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(GlobalConstants.ENABLE_TOAST_SETTING, enable);
        editor.apply();
    }

    /**
     * Задает флаг необходимости создать файл response об обновлении ПО
     *
     * @param context
     * @param requestTimeStamp
     */
    public static void setSoftwareUpdateRequestTimestamp(Context context, long requestTimeStamp) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putLong(GlobalConstants.PTK_SOFTWARE_UPDATE_REQUEST_TIMESTAMP, requestTimeStamp);
        editor.apply();
    }

    /**
     * Возвращает флаг необходимости создать файл response об обновлении ПО
     * По умолчанию false
     *
     * @param context
     * @return
     */
    public static long getSoftwareUpdateRequestTimestamp(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getLong(GlobalConstants.PTK_SOFTWARE_UPDATE_REQUEST_TIMESTAMP, 0);
    }

    /**
     * Возвращает флаг необходимости создать файл response об обновлении ПО
     * По умолчанию false
     *
     * @param context
     * @return
     */
    public static boolean isSoftwareUpdatedFlag(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalConstants.PTK_SOFTWARE_UPDATED, false);
    }

    /**
     * Задает флаг необходимости создать файл response об обновлении ПО
     *
     * @param context
     * @param flag
     */
    public static void setSoftwareUpdatedFlag(Context context, boolean flag) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(GlobalConstants.PTK_SOFTWARE_UPDATED, flag);
        editor.apply();
    }

    /**
     * Возвращает текущее состояние флага использования SAM-модуля
     *
     * @param context
     * @return
     */
    public static boolean isSamModuleUseEnable(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        boolean state = preferences.getBoolean(GlobalConstants.USE_SAM_MODULE, true);
        return state;
    }

    /**
     * Задает флаг использования SAM-модуля
     *
     * @param context
     * @param state
     */
    public static void setSamModuleUseEnable(Context context, boolean state) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(GlobalConstants.USE_SAM_MODULE, state);
        editor.apply();
    }

    /**
     * Возвращает номер используемого SAM-модуля (1 или 2)
     *
     * @param context
     * @return
     */
    public static SamSlot getSamModuleSlot(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        int samSlotNumber = preferences.getInt(GlobalConstants.SAM_MODULE_NUMBER, 1);
        if (samSlotNumber != 2) samSlotNumber = 1;
        return SamSlot.getByRealNumber(samSlotNumber);
    }

    /**
     * Задает заводской номер Фискальника
     *
     * @param context
     * @param serialNumber
     */
    public static void setSerialNumber(Context context, String serialNumber) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putString(GlobalConstants.FISCAL_SERIAL_NUMBER, serialNumber);
        Logger.trace(TAG, "setSerialNumber(" + serialNumber + ")");
        editor.apply();
    }

    /**
     * Возвращает заводской номер Фискальника
     *
     * @param context
     * @return
     */
    public static String getSerialNumber(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        Object serialNumber = preferences.getAll().get(GlobalConstants.FISCAL_SERIAL_NUMBER);
        String res = serialNumber == null ? null : String.valueOf(serialNumber);
        Logger.trace(TAG, "getSerialNumber(): " + res);
        return res;
    }

    /**
     * Задает заводской модель Фискальника
     *
     * @param context
     * @param model
     */
    public static void setModel(Context context, String model) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putString(GlobalConstants.FISCAL_MODEL, model);
        editor.apply();
        Logger.trace(TAG, "setModel(" + model + ")");
    }

    /**
     * Возвращает заводской модель Фискальника
     *
     * @param context
     * @return
     */
    public static String getModel(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        Object model = preferences.getAll().get(GlobalConstants.FISCAL_MODEL);
        String res = model == null ? null : String.valueOf(model);
        Logger.trace(TAG, "getModel(): " + res);
        return res;
    }

    /**
     * Задает номер используемого SAM-модуля (1 или 2)
     *
     * @param context
     * @param samSlot
     */
    public static void setSamModuleSlot(Context context, SamSlot samSlot) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putInt(GlobalConstants.SAM_MODULE_NUMBER, samSlot.getRealNumber());
        editor.apply();
    }

    /**
     * Возвращает текущее состояние флага использования Аварйиного режима
     *
     * @param context
     * @return
     */
    public static boolean isEmergencyModeUseEnable(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalConstants.USE_EMERGENCY_MODE, false);
    }

    /**
     * Задает флаг использования Аварйиного режима
     *
     * @param context
     * @param state
     */
    public static void setEmergencyModeUseEnable(Context context, boolean state) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(GlobalConstants.USE_EMERGENCY_MODE, state);
        editor.apply();
    }

    /**
     * Проверяет, использовать ли звуковой сигнал при удачном считывании БСК/ПД
     *
     * @param context
     * @return
     */
    public static boolean isSoundOnReadBskSuccesEnable(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalConstants.PLAY_SOUND_ON_READ_BSK_SUCCESS, false);
    }

    /**
     * Задает флаг, использовать ли звуковой сигнал при удачном считывании
     * БСК/ПД
     *
     * @param context
     * @param state
     */
    public static void setSoundReadBskSuccesEnable(Context context, boolean state) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(GlobalConstants.PLAY_SOUND_ON_READ_BSK_SUCCESS, state);
        editor.apply();
    }

    /**
     * Проверяет, использовать ли звуковой сигнал при НЕудачном считывании
     * БСК/ПД
     *
     * @param context
     * @return
     */
    public static boolean isSoundReadBskErrorEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalConstants.PLAY_SOUND_ON_READ_BSK_ERROR, false);
    }

    /**
     * Задает флаг, использовать ли звуковой сигнал при НЕудачном считывании
     * БСК/ПД
     *
     * @param context
     * @param state
     */
    public static void setSoundReadBskErrorEnable(Context context, boolean state) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(GlobalConstants.PLAY_SOUND_ON_READ_BSK_ERROR, state);
        editor.apply();
    }

    /**
     * Возвращает имя файл, который будем воспроизводить при удачном/неудачном
     * считывании ПД
     *
     * @param context
     * @param beepType
     * @return
     */
    public static String getBeepFilename(Context context, BeepType beepType) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getString(beepType.getTypeValue(), null);
    }

    /**
     * Задает текущий файл звука удачного/неудачного считывания билета
     *
     * @param context
     * @param beeType
     * @param filename
     */
    public static void setBeppFileName(Context context, BeepType beeType, String filename) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putString(beeType.getTypeValue(), filename).apply();
    }

    /**
     * Возвращает mac-адрес чекового принтера
     *
     * @param context
     * @return
     */
    public static String getPrinterMacAddress(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String printerMacAddress = preferences.getString(GlobalConstants.PrinterMacAddress, null);
        Logger.trace(TAG, "getPrinterMacAddress(): " + String.valueOf(printerMacAddress));
        return printerMacAddress;
    }

    /**
     * Задает mac-address чекового принтера
     *
     * @param context
     * @param mac
     */
    public static void setPrinterMacAddress(Context context, String mac) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        Logger.info(SharedPreferencesUtils.class, "setPrinterMacAddress(" + mac + ")");
        editor.putString(GlobalConstants.PrinterMacAddress, mac).apply();
    }

    /**
     * Возвращает идентификатор IPos-терминала
     *
     * @param context
     */
    @NonNull
    public static String getPosTerminalId(@NonNull final Context context) {
        return context
                .getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE)
                .getString(GlobalConstants.PosTerminalId, "");
    }

    /**
     * Задает идентификатор IPos-терминала
     *
     * @param context
     * @param id
     */
    public static void setPosTerminalId(@NonNull final Context context, @NonNull final String id) {
        context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE)
                .edit()
                .putString(GlobalConstants.PosTerminalId, id)
                .apply();
    }

    /**
     * Возвращает mac-адрес IPos-терминала
     *
     * @param context
     * @return
     */
    public static String getPosMacAddress(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getString(GlobalConstants.PosMacAddress, null);
    }

    /**
     * Задает mac-address IPos-терминала
     *
     * @param context
     * @param mac
     */
    public static void setPosMacAddress(Context context, String mac) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putString(GlobalConstants.PosMacAddress, mac).apply();
    }

    /**
     * Возвращает порт IPos-терминала
     *
     * @param context
     * @return
     */
    public static int getPosPort(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getInt(GlobalConstants.PosPort, GlobalConstants.POS_PORT_DEFAULT);
    }

    /**
     * Задает порт IPos-терминала
     *
     * @param context
     * @param port
     */
    public static void setPosPort(Context context, int port) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putInt(GlobalConstants.PosPort, port).apply();
    }

    /**
     * Возвращает дату выгрузки данных о контроле и продаже ПД (мс)
     *
     * @param context
     * @return
     */
    public static long getGetEventRespDateTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getLong(GlobalConstants.GET_EVENT_RESP_DATETIME, 0);
    }

    /**
     * Задает дату выгрузки данных о контроле и продаже ПД (мс)
     *
     * @param context
     * @param dateTime
     */
    public static void setGetEventRespDateTime(Context context, long dateTime) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putLong(GlobalConstants.GET_EVENT_RESP_DATETIME, dateTime).apply();
    }

    /**
     * Возвращает дату подключения к АРМ «Загрузки данных» (мс)
     *
     * @param context
     * @return
     */
    public static long getARMConnectedDateTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getLong(GlobalConstants.ARM_CONNECTED_DATETIME, 0);
    }

    /**
     * Задает дату подключения к АРМ «Загрузки данных» (мс)
     *
     * @param context
     * @param dateTime
     */
    public static void setARMConnectedDateTime(Context context, long dateTime) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putLong(GlobalConstants.ARM_CONNECTED_DATETIME, dateTime).apply();
    }

    public static void setPrinterMode(Context context, int mode) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putInt(GlobalConstants.PRINTER_MODE, mode).apply();
    }

    public static int getPrinterMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getInt(GlobalConstants.PRINTER_MODE, BuildConfig.USE_REAL_DEVICES_BY_DEFAULT ? PrinterManager.PRINTER_MODE_MOEBIUS_REAL : PrinterManager.PRINTER_MODE_FILE);
        //https://aj.srvdev.ru/browse/CPPKPP-25792 для релиза 2 выдаем по умолчанию файловый принтер
//        return preferences.getInt(GlobalConstants.PRINTER_MODE, Printer.PRINTER_MODE_FILE);
    }

    public static void setСardReadTimeToast(Context context, boolean noEKLZMode) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(GlobalConstants.CARD_READ_TIME_TOAST, noEKLZMode).apply();
    }

    public static boolean getCardReadTimeToast(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalConstants.CARD_READ_TIME_TOAST, false);
    }

    /**
     * Метод для получения типа POS терминала.
     *
     * @param context
     * @return тип POS терминала.
     * @see PosType
     */
    @Deprecated
    @NonNull
    public static PosType getPosTerminalType(@NonNull final Context context) {
        // В будущем перенести в PrivateSettings или в CommonSettings
        //удалим старые значения
        context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit().remove(GlobalConstants.POS_TERMINAL_TYPE_OLD).apply();
        context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit().remove(GlobalConstants.POS_TERMINAL_TYPE_OLD2).apply();
        context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit().remove(GlobalConstants.POS_TERMINAL_SIMULATOR).apply();
        int value = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE)
                .getInt(GlobalConstants.POS_TERMINAL_TYPE, BuildConfig.DEBUG ? PosType.DEFAULT.getValue() : PosType.INGENICO.getValue());
        if (value == PosType.DEFAULT.getValue() && BuildConfig.DISABLE_STUB_POS)
            value = PosType.INGENICO.getValue();
        return PosType.from(value);
    }

    /**
     * Метод для установления типа POS терминала.
     *
     * @param context
     * @param posType тип терминала.
     * @see PosType
     */
    @Deprecated
    public static void setPosTerminalType(@NonNull final Context context, @NonNull final PosType posType) {
        // В будущем перенести в PrivateSettings или в CommonSettings
        context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE)
                .edit()
                .putInt(GlobalConstants.POS_TERMINAL_TYPE, posType.getValue())
                .apply();
    }

    public static void setEdsType(Context context, @NonNull final EdsType edsType) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putInt(GlobalConstants.ECP_TYPE_SETTINGS, edsType.getCode());
        editor.apply();
    }

    @NonNull
    public static EdsType getEdsType(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        EdsType edsType = EdsType.valueOf(sharedPreferences.getInt(GlobalConstants.ECP_TYPE_SETTINGS, 0));
        if (edsType == null) {
            return BuildConfig.USE_REAL_DEVICES_BY_DEFAULT ? EdsType.SFT : EdsType.STUB;
        } else {
            return edsType;
        }
    }

    public static RfidType getRfidType(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return RfidType.valueOf(sharedPreferences.getString(GlobalConstants.RFID_TYPE, BuildConfig.USE_REAL_DEVICES_BY_DEFAULT ? RfidType.REAL.name() : RfidType.FILE.name()));
    }

    public static void setRfidType(Context context, @NonNull RfidType rfidType) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putString(GlobalConstants.RFID_TYPE, rfidType.name()).apply();
    }

    public static BarcodeType getBarcodeType(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return BarcodeType.valueOf(sharedPreferences
                .getString(GlobalConstants.BARCODE_TYPE, BuildConfig.USE_REAL_DEVICES_BY_DEFAULT ? BarcodeType.MDI3100.name() : BarcodeType.FILE.name()));
    }

    public static void setBarcodeType(Context context, @NonNull BarcodeType type) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putString(GlobalConstants.BARCODE_TYPE, type.name()).apply();
    }

    /**
     * Сохраняет время когда ПТК был заблокирован для авторизации из-за ввода неверного пина несколько раз подрят
     */
    public static void setTimeLockedAccess(Context context, @NonNull Date date) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putLong(GlobalConstants.timeLockedAccess, date.getTime()).apply();
    }

    /**
     * Вернет время когда ПТК был заблокирован для авторизации из-за ввода неверного пина несколько раз подрят
     */
    public static Date getTimeLockedAccess(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        Date out = null;
        long time = sharedPreferences.getLong(GlobalConstants.timeLockedAccess, -1);
        if (time != -1) out = new Date(time);
        return out;
    }

    /**
     * Вернет максимально разрешенное количество файлов в папке с бекапами
     *
     * @param context
     * @return
     */
    public static int getMaxFileCountInBackupDir(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(GlobalConstants.MAX_FILES_COUNT_IN_BACKUP_DIR, 2);
    }

    /**
     * Сохранит максимально разрешенное количество файлов в папке с бекапами
     *
     * @param context
     * @param value
     */
    public static void setMaxFileCountInBackupDir(Context context, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putInt(GlobalConstants.MAX_FILES_COUNT_IN_BACKUP_DIR, value).apply();
    }

    /**
     * Задает дату и время отправления для контролируемого трансферного рейса
     *
     * @param context
     * @param transferDepartureDateTime
     */
    public static void setTransferDepartureDateTime(Context context, @Nullable Date transferDepartureDateTime) {
        long transferDepartureDateTimeValue = 0;
        if (transferDepartureDateTime != null) {
            transferDepartureDateTimeValue = transferDepartureDateTime.getTime();
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putLong(GlobalConstants.TRANSFER_DEPARTURE_DATE_TIME, transferDepartureDateTimeValue);
        editor.apply();
    }

    /**
     * Возвращает дату и время отправления для контролируемого трансферного рейса
     * По умолчанию null
     *
     * @param context
     * @return
     */
    public static Date getTransferDepartureDateTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        long transferDepartureDateTime = preferences.getLong(GlobalConstants.TRANSFER_DEPARTURE_DATE_TIME, 0);
        Date result = null;
        if (transferDepartureDateTime != 0) {
            result = new Date(transferDepartureDateTime);
        }
        return result;
    }

    /**
     * Задает дату и время последнего обновления CommonSettingsStorage
     *
     * @param context
     * @param dateTime
     */
    public static void setCommonSettingsLastUpdate(Context context, @Nullable Date dateTime) {
        long dateTimeValue = 0;
        if (dateTime != null) {
            dateTimeValue = dateTime.getTime();
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putLong(GlobalConstants.COMMON_SETTINGS_LAST_UPDATE, dateTimeValue);
        editor.apply();
    }

    /**
     * Возвращает дату и время последнего обновления CommonSettingsStorage
     * По умолчанию null
     *
     * @param context
     * @return
     */
    @Nullable
    public static Date getCommonSettingsLastUpdate(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        long dateTime = preferences.getLong(GlobalConstants.COMMON_SETTINGS_LAST_UPDATE, 0);
        Date result = null;
        if (dateTime != 0) {
            result = new Date(dateTime);
        }
        return result;
    }
}
