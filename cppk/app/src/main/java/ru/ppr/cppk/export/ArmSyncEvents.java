package ru.ppr.cppk.export;

/**
 * Типы событий возникающие в процессе синхронизации с АРМ
 *
 * @author Григорий
 */
public enum ArmSyncEvents {

    Unknown(0, "Неизвестное событие") {
    },

    Connected(1, "Подключено") {
    },

    SetTimeReqDetected(2, "Зафиксирован запрос на корректировку времени ПТК") {
    },

    GetTimeReqDetected(3, "Зафиксирован запрос текущего времени ПТК") {
    },

    SetPrivateSettingsReqDetected(4, "Зафиксирован запрос на установку частных настроек ПТК") {
    },

    GetPrivateSettingsReqDetected(5, "Зафиксирован запрос на получение частных настроек ПТК") {
    },

    SetCommonSettingsReqDetected(6, "Зафиксирован запрос на установку общих настроек ПТК") {
    },

    GetLastShiftReqDetected(7, "Зафиксирован запрос на получение данных о последней смене") {
    },

    GetEventsReqDetected(8, "Зафиксирован запрос данных о последних событиях на ПТК") {
    },

    SecurityDbUpdataReqDetected(9, "Зафиксирован запрос на обновление базы Security") {
    },

    RdsDbUpdataReqDetected(10, "Зафиксирован запрос на обновление базы RDS") {
    },

    UpdatePoReqDetected(11, "Зафиксирован запрос на обновление ПО") {
    },

    TransmissionCompleteDetected(12, "Зафиксирован запрос на обновление ключей ЭЦП") {
    },

    Disconnected(13, "Отключено") {
    },

    Stopped(14, "Сервис Синхронизации остановлен") {
    },

    ShiftEventsRespCreated(15, "Отчет о сменах сформирован") {
    },

    TicketControlsRespCreated(16, "Отчет о событиях контроля сформирован") {
    },

    TicketSalesRespCreated(17, "Отчет о событиях продаж сформирован") {
    },

    SigCreateReady(18, "Создание файла подписи завершено") {
    },

    GetEventsRespCreated(19, "Сформирован отчет о последних событиях на ПТК (getEvents_resp.bin)") {
    },

    RdsDbUpdateReady(20, "Обновление НСИ завершено") {
    },

    SecurityDbUpdateReady(21, "Обновление Базы безопасности и стоплистов завершено") {
    },

    PoUpdateReady(22, "Обновление ПО завершено") {
    },

    GetLastShiftRespCreated(23, "Сбор данных о последней смене завершен") {
    },

    GetPrivateSettingsRespCreated(24, "Пакет данных о частных настройках ПТК сформирован") {
    },

    GetCurrentTimeRespCreated(25, "Пакет данных о текущем времени ПТК сформирован") {
    },

    SetPrivateSettingsRespCreated(26, "Изменение частных настроек ПТК завершено") {
    },

    PullSftRespCreated(27, "Обновление ключей ЭЦП завершено") {
    },

    SetCommonSettingsRespCreated(28, "Изменение общих настроек ПТК завершено") {
    },

    Runned(29, "Сервис Синхронизации запущен") {
    },

    GetBackupReqDetected(30, "Зафиксирован запрос на получение бекапа ПТК") {
    },

    TestTicketsRespCreated(31, "Отчет о событиях печати тестовых ПД сформирован") {
    },

    SyncFinishedDetected(32, "Зафиксирован запрос успешного завершения синхронизации") {
    },

    GetStateReqDetected(33, "Зафиксирован запрос состояния") {
    },

    SigCreateStart(34, "Стартуем создание файла подписи...") {
    },

    AllEventRespCreateStart(35, "Стартуем создание файла getEvents_resp.bin...") {
    },

    ShiftEventsCreateStart(36, "Стартуем создание отчета о сменах...") {
    },

    TicketControlsCreateStart(37, "Стартуем создание отчета о событиях контроля...") {
    },

    TicketSalesCreateStart(38, "Стартуем создание отчета о событиях продаж...") {
    },

    TestTicketsCreateStart(39, "Стартуем создание отчета о событиях пеачати тестового ПД...") {
    },

    SetTimeRespCreated(40, "Отчет о корректировке времени на ПТК сформирован") {
    },

    PublicKeyUnzipReady(41, "Распаковка архива с ключами SFT завершена") {
    },

    PublicKeyDeleteReqDetected(42, "Зафиксирован запрос на удаление устаревших файлов SFT") {
    },

    OldSftFileDeleteReady(43, "Удаляем устаревший файл SFT") {
    },

    PublicKeyDeleteReady(44, "Удаление устаревших файлов SFT завершено") {
    },

    TakeLicsStart(45, "Пробуем подхватить новые лицензии...") {
    },

    TransportLicFolderClearStart(46, "Очищаем транспортную папку с лицензиями...") {
    },

    PullSftStart(47, "Перезапускаем SFT...") {
    },

    BackupCreated(48, "Создание бекапа ПТК завершено") {
    },

    BackupSigCreateStart(49, "Стартуем создание подписи для файла бекапа...") {
    },

    ClearSftLogFolder(50, "Удаляем логи SFT") {
    },

    BackupRespCreateStart(51, "Стартуем создание файла getBackup_resp.bin...") {
    },

    CreateFileSigStart(52, "Стартуем создание файла подписи...") {
    },

    CreateFileSigFinish(53, "Создание файла подписи завершено") {
    },

    CreateSftFilesListFileStart(54, "Стартуем создание файла со списком файлов в папке SftTransport/in...") {
    },

    CreateSftFilesListFileReady(55, "Завершили создание файла со списком файлов в папке SftTransport/in") {
    },

    TicketReturnsCreateStart(56, "Стартуем создание отчета о событиях аннулирования ПД...") {
    },

    TicketReturnsRespCreated(57, "Отчет о событиях аннулирования сформирован...") {
    },

    MonthClosuresCreateStart(58, "Стартуем создание отчета о событиях закрытия месяца...") {
    },

    MonthClosuresRespCreated(59, "Отчет о событиях закрытия месяца сформирован...") {
    },

    TicketPaperRollsCreateStart(60, "Стартуем создание отчета о событиях смены билетной ленты...") {
    },

    TicketPaperRollsRespCreated(61, "Отчет о событиях смены билетной ленты сформирован...") {
    },

    BankTransactionsCreateStart(62, "Стартуем создание отчета о банковских транзакциях...") {
    },

    BankTransactionsRespCreated(63, "Отчет о событиях банковских транзакций сформирован...") {
    },

    TicketResignsCreateStart(64, "Стартуем создание отчета о переподписанных билетах...") {
    },

    TicketResignsRespCreated(65, "Отчет о событиях переподписанных билетов сформирован...") {
    },

    ServiceSalesCreateStart(66, "Стартуем создание отчета о проданных услугах...") {
    },

    ServiceSalesRespCreated(67, "Отчет о проданных услугах сформирован...") {
    },

    CreateFileFinish(68, "Создание файла завершено") {
    },

    ClearFatalsLogFolder(69, "Удаляем логи crashReports") {
    },

    ClearZebraLogFolder(70, "Удаляем логи ФР") {
    },

    FinePaidEventsCreateStart(71, "Стартуем создание отчета о проданных штрафах...") {
    },

    FinePaidEventsRespCreated(72, "Отчет о проданных штрафах сформирован...") {
    },

    ServiceTicketControlsRespCreateStart(73, "Стартуем создание отчета о контроле сервисных карт...") {
    },

    ServiceTicketControlsRespCreated(74, "Отчет о контроле сервисных карт сформирован...") {
    },

    ;

    /**
     * Код события
     */
    private int сode;

    /**
     * Описание события
     */
    private String description;

    ArmSyncEvents(int сode, String description) {
        this.сode = сode;
        this.description = description;
    }

    /**
     * type of SmartCard
     *
     * @return int
     */
    public int getСode() {
        return сode;
    }

    static public ArmSyncEvents getTypeByCode(int сode) {
        for (ArmSyncEvents type : ArmSyncEvents.values()) {
            if (type.getСode() == сode) {
                return type;
            }
        }
        return ArmSyncEvents.Unknown;
    }

    /**
     * description of SmartCard
     *
     * @return String
     */
    public String getDescription() {
        return description;
    }

}
