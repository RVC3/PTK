package ru.ppr.cppk;

import ru.ppr.cppk.ui.activity.ArmConnectedStateActivity;
import ru.ppr.cppk.ui.activity.BluetoothDeviceSearchActivity;
import ru.ppr.cppk.ui.activity.LockScreenActivity;

public class GlobalConstants {

    /**
     * Костылёк, позволяющий быстро отключить автоматическое закрытие смены
     */
    public static final boolean ENABLE_AUTO_CLOSE_SHIFT = true;

    /* ---------------------------------------- *
     * -----   Настройки SystemBarActivity -----*
     * ---------------------------------------- */
    /**
     * Название activity на которых можно находиться без заданного userRoleId.
     */
    public static final String[] defaultUserIdActivitysEnabled = {SplashActivity.class.getName(), SetUserIdActivity.class.getName(), EnterPinActivity.class.getName(), ArmConnectedStateActivity.class.getName(), PrinterBindingActivity.class.getName(), PosBindingActivity.class.getName(), BluetoothDeviceSearchActivity.class.getName(), RootAccessRequestActivity.class.getName()};

    /**
     * Список activity на которых можно находиться с флагом Globals.isSyncNow=false (непроинициализированное SFT)
     */
    public static final String[] notAllInitedActivitysEnabled = {SplashActivity.class.getName(), SetUserIdActivity.class.getName(), PrinterBindingActivity.class.getName(), PosBindingActivity.class.getName(), BluetoothDeviceSearchActivity.class.getName(), RootAccessRequestActivity.class.getName()};
    /**
     * Название activity на которых можно находиться без заданного PtkNumber.
     */
    public static final String[] defaultPtkNumberActivitysEnabled = {SplashActivity.class.getName(), SetUserIdActivity.class.getName(), RootAccessRequestActivity.class.getName(), BluetoothDeviceSearchActivity.class.getName()};
    /**
     * Название activity на которых можно находиться без заданного Серийного номера.
     */
    public static final String[] defaultSerialNumberActivitysEnabled = {SplashActivity.class.getName(), SetUserIdActivity.class.getName(), RootAccessRequestActivity.class.getName(), BluetoothDeviceSearchActivity.class.getName(), PrinterBindingActivity.class.getName()};
    /**
     * Название activity на которых может быть не запущен сервис синхронизации с ARM
     */
    public static final String[] notRunningArmServiceActivitysEnabled = {LockScreenActivity.class.getName(), SplashActivity.class.getName(), SetUserIdActivity.class.getName(), PrinterBindingActivity.class.getName(), PosBindingActivity.class.getName(), RootAccessRequestActivity.class.getName(), BluetoothDeviceSearchActivity.class.getName()};


    /* ---------------------------------------- *
     * ------  Shared Preferenses Fields ------ *
     * ---------------------------------------- */
    public static final String SHARED_PREFERENCE = "AppSettings";


    public static final String RFID_TYPE = "RFID_TYPE";
    public static final String BARCODE_TYPE = "BARCODE_TYPE";

    /**
     * название поля, отвечающего за сохраниение даты и времени последнего обновления CommonSettingsStorage
     */
    public static final String COMMON_SETTINGS_LAST_UPDATE = "COMMON_SETTINGS_LAST_UPDATE";
    /**
     * название поля, отвечающего за сохраниение даты и времени отправления для контролируемого трансферного рейса
     */
    public static final String TRANSFER_DEPARTURE_DATE_TIME = "TRANSFER_DEPARTURE_DATE_TIME";
    /**
     * название поля, отвечающего за сохраниение Станции привязки ПТК
     */
    public static final String currentBindingStation = "CurrentBindingStation";

    /**
     * название поля, отвечающего за сохраниение времени совершения блокировки ПТК из-за неверно введенного несколько раз ПИНкода
     */
    public static final String timeLockedAccess = "TimeLockedAccess";

    /**
     * название поля, отвечающего за сохраниение максимально разрешенного количества файлов в папке с бекапами
     */
    public static final String MAX_FILES_COUNT_IN_BACKUP_DIR = "MAX_FILES_COUNT_IN_BACKUP_DIR";

    /**
     * включает проверку подписей на билетах и файлах полученных с АРМ
     */
    public static final String ECP_CHECK_SETTINGS = "EcpCgeckState";
    /**
     * Активирует элементы интерфейса второго этапа
     */
    public static final String SALE_INTERFACE_ENABLED = "SaleInterfaceEnabled";
    /**
     * Задает флаг необходимости создать файл респонса об обновлении ПО ПТК
     */
    public static final String PTK_SOFTWARE_UPDATED = "PtkSoftwareUpdated";
    /**
     * Суффикс от запроса на обновление ПО
     */
    public static final String PTK_SOFTWARE_UPDATE_REQUEST_TIMESTAMP = "PTK_SOFTWARE_UPDATE_REQUEST_TIMESTAMP";
    /**
     * Разрешение показывать тосты с ошибками
     */
    public static final String ENABLE_TOAST_SETTING = "ErrorToast";
    /**
     * Разрешение использовать образы карт вместо реальных
     */
    public static final String ENABRE_CARD_IMAGES_SETTING = "EnableCardImagesSetting";
    /**
     * Флаг наличия билетной ленты в принтере
     */
    public static final String STATE_TICKET_TAPE_SETTING = "StateTicketTape";
    /**
     * Флаг работы с sam-модулем
     */
    public static final String USE_SAM_MODULE = "UseSAMModule";
    /**
     * Номер используемого sam-модуля
     */
    public static final String SAM_MODULE_NUMBER = "SAM_MODULE_NUMBER";
    /**
     * Флаг выхода в аварийный режим
     */
    public static final String USE_EMERGENCY_MODE = "USE_EMERGENCY_MODE";
    /**
     * Идентификатор в SharedPreferences флага использования звукового сигнала при удачном считывании БСК/ПД
     */
    public static final String PLAY_SOUND_ON_READ_BSK_SUCCESS = "PLAY_SOUND_ON_READ_BSK_SUCCESS";
    /**
     * Идентификатор в SharedPreferences флага использования звукового сигнала при неудачном считывании БСК/ПД
     */
    public static final String PLAY_SOUND_ON_READ_BSK_ERROR = "PLAY_SOUND_ON_READ_BSK_ERROR";
    /**
     * Идентификатор в SharedPreferences MAC-адреса чекового принтера
     */
    public static final String PrinterMacAddress = "zebra_mac_address";
    /**
     * Идентификатор в SharedPreferences INN чекового принтера
     */
    public static final String CashRegisterInn = "CashRegisterINN";
    /**
     * Идентификатор в SharedPreferences EklsNumber чекового принтера
     */
    public static final String CashRegisterEklsNumber = "CashRegisterEklsNumber";
    /**
     * Идентификатор в SharedPreferences FNSerial чекового принтера
     */
    public static final String CashRegisterFnSerial = "CashRegisterFnSerial";
    /**
     * Идентификатор в SharedPreferences SerialNumber чекового принтера
     */
    public static final String CashRegisterSerialNumber = "CashRegisterSerialNumber";
    /**
     * Идентификатор в SharedPreferences Model чекового принтера
     */
    public static final String CashRegisterModel = "CashRegisterModel";
    /**
     * Идентификатор в SharedPreferences идентификатора IPos-терминала
     */
    public static final String PosTerminalId = "pos_terminal_id";
    /**
     * Идентификатор в SharedPreferences MAC-адреса IPos-терминала
     */
    public static final String PosMacAddress = "pos_mac_address";
    /**
     * Идентификатор в SharedPreferences порта IPos-терминала
     */
    public static final String PosPort = "pos_port";
    // Обновление ПТК
    /**
     * Идентификатор в SharedPreferences даты выгрузки данных о контроле и продаже ПД
     */
    public static final String GET_EVENT_RESP_DATETIME = "GET_EVENT_RESP_DATETIME";
    /**
     * Идентификатор в SharedPreferences даты подключения к АРМ «Загрузки данных»
     */
    public static final String ARM_CONNECTED_DATETIME = "GET_EVENT_RESP_DATETIME";
    /**
     * Режим принтера
     */
    public static final String PRINTER_MODE = "PRINTER_MODE";
    /**
     * Флаг включения уведомления о потраченном на считывание карты времени
     */
    public static final String CARD_READ_TIME_TOAST = "CARD_READ_TIME_TOAST";
    /**
     * Флаг включения сбора статистики о потраченном на считывание карты времени
     */
    public static final String CARD_READ_TIME_STATISTICS = "CARD_READ_TIME_STATISTICS";
    /**
     * Флаг включения симулятора банковского терминала
     */
    @Deprecated //больше не используется
    public static final String POS_TERMINAL_SIMULATOR = "POS_TERMINAL_SIMULATOR";
    /**
     * Флаг типа банковского терминала.
     *
     * @see ru.ppr.cppk.pos.PosType
     */
    @Deprecated //использовать POS_TERMINAL_TYPE
    public static final String POS_TERMINAL_TYPE_OLD = "POS_TERMINAL_TYPE";
    /**
     * Флаг типа банковского терминала.
     *
     * @see ru.ppr.cppk.pos.PosType
     */
    @Deprecated //использовать POS_TERMINAL_TYPE
    public static final String POS_TERMINAL_TYPE_OLD2 = "POS_TERMINAL_TYPE_FROM_221";
    /**
     * Флаг типа банковского терминала.
     *
     * @see ru.ppr.cppk.pos.PosType
     */
    public static final String POS_TERMINAL_TYPE = "POS_TERMINAL_TYPE_FROM_221_2";
    /**
     * Режим работы ПТК
     */
    public static final String WORK_MODE = "WorkMode";
    /**
     * Последнее обновление электронного списка
     */
    public static final String ELECTROL_LIST_UPDATE_DATE = "ElectronListUpdate";

    /**
     * Поле для хранения типа текущей ЭЦП
     */
    public static final String ECP_TYPE_SETTINGS = "ECP_TYPE_SETTINGS";
    /**
     * Поле хранения Заводского номера Фискальника
     */
    public static final String FISCAL_SERIAL_NUMBER = "PtkSerialNumber";
    /**
     * Поле хранения Модели фискальника
     */
    public static final String FISCAL_MODEL = "FiscalModel";

    /**
     * Задержка автоматического выключения ридера после обращения к нему, ms
     */
    public static final long AUTO_POWER_OFF_DELAY = 10000;

    /* ---------------------------------------- *
     * ---- Названия apk файлов в assets ------ *
     * ---------------------------------------- */
    /**
     * название apk файл менеджера
     */
    public static final String FILE_MANAGER_APK_NAME = "Total-Commander.apk";
    /**
     * название package файл менеджера
     */
    public static final String FILE_MANAGER_PACKAGE = "com.ghisler.android.TotalCommander";
    /**
     * название apk CpcHdkConeSample
     */
    public static final String CpcHdkConeSampleApkName = "cpchdkconesample-1.3.0.apk";


    /**
     * Путь до папки с логом критических ошибок
     */
    public static final String SUCCESS_BEEP_PATH = "Sounds/success_beep";
    /**
     * Путь до папки с звуками при неудачно считывании
     */
    public static final String FAIL_BEEP_PATH = "Sounds/fail_beep";

    public static final String OPTICON_BAUDRATE_SETTING_NAME = "OpticonBaudrate";
    public static final String OPTICON_BAUDRATE_DEFAULT = "115200";

    public static final int SECOND_IN_DAY = 24 * 60 * 60;
    public static final long MILLISECOND_IN_DAY = 24 * 60 * 60 * 1000;
    public static final int ECP_SIZE_BYTE = 64;
    /**
     * Длина номера ключа ЭЦП в байтах
     */
    public static final int ECP_KEY_BYTES = 4;
    public static final int BYTE_IN_BLOCK = 16;

    // Activitys code
    public static final int READ_BARCODE_ACTIVITY = 1;
    public static final int READ_RFID_ACTIVITY = 2;

    /**
     * Порт по-умолчанию для IPos-терминала
     */
    public static final int POS_PORT_DEFAULT = 9301;

}

